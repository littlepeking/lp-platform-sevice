/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.backend.core.inbound;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.base.Loc;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotAttribute;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.AllocationStrategy;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.core.inventory.InventoryOperations;
import com.enhantec.wms.backend.core.outbound.OutboundUtilHelper;
import com.enhantec.wms.backend.core.outbound.allocations.AllocationExecutor;
import com.enhantec.wms.backend.core.outbound.allocations.OrderDetailAllocInfo;
import com.enhantec.wms.backend.core.outbound.allocations.strategies.HardAllocationService;
import com.enhantec.wms.backend.core.outbound.allocations.strategies.SoftAllocationService;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class InboundOperations {

    public void receivingById(String id, BigDecimal qty){


        if(StringUtils.isEmpty(id)) throw new FulfillLogicException("托盘号不能为空");

        Map<String,String> receiptHashMap = Receipt.findUnreceivedRDByLPN(id, true);

        String receiptKey = receiptHashMap.get("RECEIPTKEY");

        String receiptLineNumber = receiptHashMap.get("RECEIPTLINENUMBER");


        qty = qty != null && qty.compareTo(BigDecimal.ZERO) > 0 ? qty : new BigDecimal(receiptHashMap.get("QTYEXPECTED"));

        String STORERKEY=receiptHashMap.get("STORERKEY");
        String SKU=receiptHashMap.get("SKU");
        String TOLOT=receiptHashMap.get("TOLOT");
        String TOID=receiptHashMap.get("TOID");
        String TOLOC= UtilHelper.getString(receiptHashMap.get("TOLOC"), "STAGE");
        String CONDITIONCODE=UtilHelper.getString(receiptHashMap.get("CONDITIONCODE"), "OK");

        TOLOT= LotAttribute.findMatchedLot(TOLOT
                ,receiptHashMap.get("STORERKEY"),receiptHashMap.get("SKU")
                ,receiptHashMap.get("LOTTABLE01"),receiptHashMap.get("LOTTABLE02"),receiptHashMap.get("LOTTABLE03"),receiptHashMap.get("LOTTABLE04"),receiptHashMap.get("LOTTABLE05"),receiptHashMap.get("LOTTABLE06"),receiptHashMap.get("LOTTABLE07"),receiptHashMap.get("LOTTABLE08"),receiptHashMap.get("LOTTABLE09"),receiptHashMap.get("LOTTABLE10"),receiptHashMap.get("LOTTABLE11"),receiptHashMap.get("LOTTABLE12")
        );
        if (StringUtils.isEmpty(TOLOT))
        {
            TOLOT= LotAttribute.createLot(receiptHashMap.get("STORERKEY"),receiptHashMap.get("SKU")
                    ,receiptHashMap.get("LOTTABLE01"),receiptHashMap.get("LOTTABLE02"),receiptHashMap.get("LOTTABLE03"),receiptHashMap.get("LOTTABLE04"),receiptHashMap.get("LOTTABLE05"),receiptHashMap.get("LOTTABLE06"),receiptHashMap.get("LOTTABLE07"),receiptHashMap.get("LOTTABLE08"),receiptHashMap.get("LOTTABLE09"),receiptHashMap.get("LOTTABLE10"),receiptHashMap.get("LOTTABLE11"),receiptHashMap.get("LOTTABLE12")
            );
        }
        //---写入LOT-----------------------------------------
        if (DBHelper.getCount("select count(1) from LOT where Lot=?",new Object[]{TOLOT})==0)
        {
            LinkedHashMap<String,String> Lot=new LinkedHashMap<>();
            Lot.put("addwho", EHContextHelper.getUser().getUsername());
            Lot.put("editwho", EHContextHelper.getUser().getUsername());
            Lot.put("LOT",  TOLOT);
            Lot.put("STORERKEY", STORERKEY);
            Lot.put("SKU", SKU);
            Lot.put("qty", qty.toString());
            if (!CONDITIONCODE.equals("OK"))
            {
                Lot.put("QTYONHOLD", qty.toString());
            }
            LegacyDBHelper.ExecInsert( "Lot", Lot);
        }
        else
        {
            LegacyDBHelper.ExecSql("Update lot Set qty=qty+?,QTYONHOLD=QTYONHOLD+?,editwho=?,editdate=? where Lot=?"
                    , new Object[]{qty.toString(),CONDITIONCODE.equals("OK")?"0":qty.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOLOT});
        }
        //---写入LOTXLOCXID-----------------------------------------
        if (DBHelper.getCount("select count(1) from lotxlocxid  where  Lot=? and Loc=? and ID=?",new Object[]{TOLOT,TOLOC,TOID})==0)
        {

            LinkedHashMap<String,String> lotxlocxid=new LinkedHashMap<>();
            lotxlocxid.put("addwho", EHContextHelper.getUser().getUsername());
            lotxlocxid.put("editwho", EHContextHelper.getUser().getUsername());
            lotxlocxid.put("LOT", TOLOT);
            lotxlocxid.put("LOC", TOLOC);
            lotxlocxid.put("ID", TOID);
            lotxlocxid.put("STORERKEY", STORERKEY);
            lotxlocxid.put("SKU", SKU);
            lotxlocxid.put("qty", qty.toString());
            lotxlocxid.put("STATUS", CONDITIONCODE.equals("OK")?"OK":"HOLD");
            LegacyDBHelper.ExecInsert( "lotxlocxid", lotxlocxid);
        }
        else
        {
            LegacyDBHelper.ExecSql("Update lotxlocxid Set Qty=Qty+?,editwho=?,editdate=?,STATUS=? where Lot=? and Loc=? and ID=?"
                    , new Object[]{qty.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(), CONDITIONCODE.equals("OK")?"OK":"HOLD",TOLOT,TOLOC,TOID});
        }
//        //---写入ID-----------------------------------------
        if (DBHelper.getCount("select count(1) from ID where ID=?", new Object[]{TOID})==0)
        {
            LinkedHashMap<String,String> idHashMap=new LinkedHashMap<>();
            idHashMap.put("addwho", EHContextHelper.getUser().getUsername());
            idHashMap.put("editwho", EHContextHelper.getUser().getUsername());
            idHashMap.put("ID", TOID);
            idHashMap.put("qty", qty.toString());
            idHashMap.put("STATUS", "OK");
            idHashMap.put("PACKKEY", receiptHashMap.get("PACKKEY"));
            LegacyDBHelper.ExecInsert( "id", idHashMap);
        }
        else
            DBHelper.executeUpdate( "Update ID Set Qty=Qty+?,editwho=?,editdate=? where ID=?"
                    , new Object[]{qty.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOID});


//        ---写入SKUXLOC(暂时保留这个逻辑，重构后删除)------------------------------------------
        if (DBHelper.getCount("select count(1) from SKUXLOC  where  LOC=?  and storerkey=? and Sku=?"
                , new Object[]{TOLOC,STORERKEY,SKU})==0)
        {
            String LOCATIONTYPE=DBHelper.getStringValue( "SELECT LOCATIONTYPE FROM LOC WHERE LOC=?"
                    , new Object[]{TOLOC});
            LinkedHashMap<String,String> SKUXLOC=new LinkedHashMap<>();
            SKUXLOC.put("addwho", EHContextHelper.getUser().getUsername());
            SKUXLOC.put("editwho", EHContextHelper.getUser().getUsername());
            SKUXLOC.put("STORERKEY", STORERKEY);
            SKUXLOC.put("SKU", SKU);
            SKUXLOC.put("LOC", TOLOC);
            SKUXLOC.put("qty", qty.toString());
            SKUXLOC.put("LOCATIONTYPE", LOCATIONTYPE);
            LegacyDBHelper.ExecInsert( "SKUXLOC", SKUXLOC);
        }
        else
            DBHelper.executeUpdate("UPDATE SKUXLOC SET qty=qty+?,EDITWHO=?,EDITDATE=? WHERE LOC=? AND STORERKEY=? AND SKU=?"
                    , new Object[]{qty.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOLOC,STORERKEY,SKU});
        //-----INVENTORYHOLD------------------------------------------------------------
        if (!CONDITIONCODE.equals("OK"))
        {
            String INVENTORYHOLDKEY= IdGenerationHelper.getNCounterStr("INVENTORYHOLDKEY");
            String HOLD= DBHelper.getStringValue( "SELECT HOLD FROM INVENTORYHOLD WHERE LOT=? AND LOC=? AND ID=? AND STATUS=?"
                    , new Object[] {" "," ",TOID,CONDITIONCODE});
            if (HOLD.equals(""))
            {
                LinkedHashMap<String,String> INVENTORYHOLD=new LinkedHashMap<>();
                // COMMENTS, HOURSTOHOLD, AUTORELEASEDATE, ADDDATE, ADDWHO, EDITDATE, EDITWHO
                INVENTORYHOLD.put("ADDWHO",EHContextHelper.getUser().getUsername());
                INVENTORYHOLD.put("EDITWHO",EHContextHelper.getUser().getUsername());
                INVENTORYHOLD.put("INVENTORYHOLDKEY",INVENTORYHOLDKEY);
                INVENTORYHOLD.put("LOT"," ");
                INVENTORYHOLD.put("LOC"," ");
                INVENTORYHOLD.put("ID",TOID);
                INVENTORYHOLD.put("HOLD","1");
                INVENTORYHOLD.put("STATUS",CONDITIONCODE);
                INVENTORYHOLD.put("LOC"," ");
                INVENTORYHOLD.put("DATEON", LocalDateTime.now().toString());
                INVENTORYHOLD.put("WHOON",EHContextHelper.getUser().getUsername());
                LegacyDBHelper.ExecInsert( "INVENTORYHOLD", INVENTORYHOLD);
            }
            else
            {
                if (HOLD.equals("0"))
                    LegacyDBHelper.ExecSql( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                                    + " WHERE LOT=? AND LOC=? AND ID=? AND STATUS=? AND HOLD=?"
                            , new Object[] {EHContextHelper.getUser().getUsername(),LocalDateTime.now(),"1",LocalDateTime.now().toString(),EHContextHelper.getUser().getUsername()
                                    ," "," ",TOID,CONDITIONCODE,"0"});
            }
        }
        //-----------------------------------------------------------------------
        LegacyDBHelper.ExecSql("UPDATE RECEIPTDETAIL SET QTYRECEIVED=QTYRECEIVED+?,STATUS=?,EDITWHO=?,EDITDATE=?  WHERE RECEIPTKEY=? AND RECEIPTLINENUMBER=?"
                , new Object[] {qty.toString(),"9",EHContextHelper.getUser().getUsername(),LocalDateTime.now(),receiptKey,receiptLineNumber});

        //处理收货单汇总行状态和单头状态.
        Receipt.processReceiptStatus(receiptKey);
    }

}
