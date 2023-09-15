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

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service("core.inbound.receivingById")
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class ReceivingByIdService extends WMSBaseService{

    public void execute(ServiceDataHolder serviceDataHolder){

        String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
        String lpn = serviceDataHolder.getInputDataAsMap().getString("LPN");

        if(StringUtils.isEmpty(lpn)) throw new FulfillLogicException("托盘号不能为空");

        Map<String,String> receiptHashMap = Receipt.findUnreceivedRDByLPN(receiptKey, lpn, true);

        String receiptLineNumber = receiptHashMap.get("RECEIPTLINENUMBER");

        BigDecimal QTY = serviceDataHolder.getInputDataAsMap().getDecimalValue("QTY");

        QTY = QTY != null && QTY.compareTo(BigDecimal.ZERO) > 0 ? QTY : new BigDecimal(receiptHashMap.get("QTYEXPECTED"));

        String STORERKEY=receiptHashMap.get("STORERKEY");
        String SKU=receiptHashMap.get("SKU");
        String TOLOT=receiptHashMap.get("TOLOT");
        String TOID=receiptHashMap.get("TOID");
        String TOLOC= UtilHelper.getString(receiptHashMap.get("TOLOC"), "STAGE");
        String CONDITIONCODE=UtilHelper.getString(receiptHashMap.get("CONDITIONCODE"), "OK");

        TOLOT= checkLot(TOLOT
                ,receiptHashMap.get("STORERKEY"),receiptHashMap.get("SKU")
                ,receiptHashMap.get("LOTTABLE01"),receiptHashMap.get("LOTTABLE02"),receiptHashMap.get("LOTTABLE03"),receiptHashMap.get("LOTTABLE04"),receiptHashMap.get("LOTTABLE05"),receiptHashMap.get("LOTTABLE06"),receiptHashMap.get("LOTTABLE07"),receiptHashMap.get("LOTTABLE08"),receiptHashMap.get("LOTTABLE09"),receiptHashMap.get("LOTTABLE10"),receiptHashMap.get("LOTTABLE11"),receiptHashMap.get("LOTTABLE12")
        );
        if (StringUtils.isEmpty(TOLOT))
        {
            TOLOT= createLot(receiptHashMap.get("STORERKEY"),receiptHashMap.get("SKU")
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
            Lot.put("QTY", QTY.toString());
            if (!CONDITIONCODE.equals("OK"))
            {
                Lot.put("QTYONHOLD", QTY.toString());
            }
            LegacyDBHelper.ExecInsert( "Lot", Lot);
        }
        else
        {
            LegacyDBHelper.ExecSql("Update lot Set QTY=QTY+?,QTYONHOLD=QTYONHOLD+?,editwho=?,editdate=? where Lot=?"
                    , new Object[]{QTY.toString(),CONDITIONCODE.equals("OK")?"0":QTY.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOLOT});
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
            lotxlocxid.put("QTY", QTY.toString());
            lotxlocxid.put("STATUS", CONDITIONCODE.equals("OK")?"OK":"HOLD");
            LegacyDBHelper.ExecInsert( "lotxlocxid", lotxlocxid);
        }
        else
        {
            LegacyDBHelper.ExecSql("Update lotxlocxid Set Qty=Qty+?,editwho=?,editdate=?,STATUS=? where Lot=? and Loc=? and ID=?"
                    , new Object[]{QTY.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(), CONDITIONCODE.equals("OK")?"OK":"HOLD",TOLOT,TOLOC,TOID});
        }
//        //---写入ID-----------------------------------------
        if (DBHelper.getCount("select count(1) from ID where ID=?", new Object[]{TOID})==0)
        {
            LinkedHashMap<String,String> id=new LinkedHashMap<>();
            id.put("addwho", EHContextHelper.getUser().getUsername());
            id.put("editwho", EHContextHelper.getUser().getUsername());
            id.put("ID", TOID);
            id.put("QTY", QTY.toString());
            id.put("STATUS", "OK");
            id.put("PACKKEY", receiptHashMap.get("PACKKEY"));
            LegacyDBHelper.ExecInsert( "id", id);
        }
        else
            DBHelper.executeUpdate( "Update ID Set Qty=Qty+?,editwho=?,editdate=? where ID=?"
                    , new Object[]{QTY.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOID});


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
            SKUXLOC.put("QTY", QTY.toString());
            SKUXLOC.put("LOCATIONTYPE", LOCATIONTYPE);
            LegacyDBHelper.ExecInsert( "SKUXLOC", SKUXLOC);
        }
        else
            DBHelper.executeUpdate("UPDATE SKUXLOC SET QTY=QTY+?,EDITWHO=?,EDITDATE=? WHERE LOC=? AND STORERKEY=? AND SKU=?"
                    , new Object[]{QTY.toString(),EHContextHelper.getUser().getUsername(),LocalDateTime.now(),TOLOC,STORERKEY,SKU});
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
                , new Object[] {QTY.toString(),"9",EHContextHelper.getUser().getUsername(),LocalDateTime.now(),receiptKey,receiptLineNumber});

        //处理收货单汇总行状态和单头状态.
        Receipt.processReceiptStatus(receiptKey);
    }


    public static String checkLot(String lot,
                                  String StorerKey, String Sku
            , String LOTTABLE01, String LOTTABLE02, String LOTTABLE03, String LOTTABLE04, String LOTTABLE05, String LOTTABLE06, String LOTTABLE07, String LOTTABLE08, String LOTTABLE09, String LOTTABLE10, String LOTTABLE11, String LOTTABLE12
    )
    {
        Map<String,String> lotAttribute = null;
        if (StringUtils.isEmpty(lot))
        {
            lotAttribute = DBHelper.getRecord("SELECT * FROM LOTATTRIBUTE WHERE STORERKEY=? AND SKU=?"
                            +" AND LOTTABLE01=? AND LOTTABLE02=? AND LOTTABLE03=?"
                            +" AND "+ (LOTTABLE04 == null ? " LOTTABLE04 IS NULL" : "LOTTABLE04 = '"+LOTTABLE04+"'")
                            +" AND "+ (LOTTABLE05 == null ? " LOTTABLE05 IS NULL" : "LOTTABLE05 = '"+LOTTABLE05+"'")
                            +" AND LOTTABLE06=? AND LOTTABLE07=? AND LOTTABLE08=? AND LOTTABLE09=? AND LOTTABLE10=?"
                            +" AND "+ (LOTTABLE11 == null ? " LOTTABLE11 IS NULL" : "LOTTABLE11 = '"+LOTTABLE11+"'")
                            +" AND "+ (LOTTABLE12 == null ? " LOTTABLE12 IS NULL" : "LOTTABLE12 = '"+LOTTABLE12+"'")
                    , new Object[] {StorerKey,Sku
                            ,UtilHelper.getString(LOTTABLE01," "),
                            UtilHelper.getString(LOTTABLE02," "),
                            UtilHelper.getString(LOTTABLE03," "),
                            UtilHelper.getString(LOTTABLE06," "),
                            UtilHelper.getString(LOTTABLE07," "),
                            UtilHelper.getString(LOTTABLE08," "),
                            UtilHelper.getString(LOTTABLE09," "),
                            UtilHelper.getString(LOTTABLE10," "),
                    });
            if (lotAttribute==null) return null;
        }
        else
        {
            lotAttribute=DBHelper.getRecord( "SELECT * FROM LOTATTRIBUTE WHERE LOT=?", new Object[] {lot});
            if (lotAttribute==null) throw new FulfillLogicException("系统中无此LOT("+lot+")");
        }
        if (!StorerKey.equals(UtilHelper.getString(lotAttribute.get("STORERKEY"), ""))) throw new FulfillLogicException("LOT("+lot+")货主匹配错误");
        if (!Sku.equals(UtilHelper.getString(lotAttribute.get("SKU"), ""))) throw new FulfillLogicException("LOT("+lot+")物料代码匹配错误");
        if (!UtilHelper.getString(LOTTABLE01," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE01"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE01匹配错误");
        if (!UtilHelper.getString(LOTTABLE02," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE02"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE02匹配错误");
        if (!UtilHelper.getString(LOTTABLE03," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE03"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE03匹配错误");
        if (!UtilHelper.getString(LOTTABLE04," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE04"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE04匹配错误");
        if (!UtilHelper.getString(LOTTABLE05," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE05"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE05匹配错误");
        if (!UtilHelper.getString(LOTTABLE06," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE06"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE06匹配错误");
        if (!UtilHelper.getString(LOTTABLE07," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE07"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE07匹配错误");
        if (!UtilHelper.getString(LOTTABLE08," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE08"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE08匹配错误");
        if (!UtilHelper.getString(LOTTABLE09," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE09"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE09匹配错误");
        if (!UtilHelper.getString(LOTTABLE10," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE10"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE10匹配错误");
        if (!UtilHelper.getString(LOTTABLE11," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE11"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE11匹配错误");
        if (!UtilHelper.getString(LOTTABLE12," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE12"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE12匹配错误");

        return lotAttribute.get("LOT");
    }


    private String getDateString(String dateStr){
       return StringUtils.isEmpty(dateStr)? null: dateStr;
    }

    public static String createLot( String StorerKey,String Sku
            ,String LOTTABLE01,String LOTTABLE02,String LOTTABLE03,String LOTTABLE04,String LOTTABLE05,String LOTTABLE06,String LOTTABLE07,String LOTTABLE08,String LOTTABLE09,String LOTTABLE10,String LOTTABLE11,String LOTTABLE12
    )
    {
        String LOT=IdGenerationHelper.getNCounterStrWithLength("LOT",10);
        LinkedHashMap<String,String> mLOT=new LinkedHashMap<>();
        mLOT.put("ADDWHO",EHContextHelper.getUser().getUsername());
        mLOT.put("EDITWHO",EHContextHelper.getUser().getUsername());
        mLOT.put("LOT",LOT);
        mLOT.put("STORERKEY",StorerKey);
        mLOT.put("SKU",Sku);
        mLOT.put("LOTTABLE01",UtilHelper.getString(LOTTABLE01, " "));
        mLOT.put("LOTTABLE02",UtilHelper.getString(LOTTABLE02, " "));
        mLOT.put("LOTTABLE03",UtilHelper.getString(LOTTABLE03, " "));
        mLOT.put("LOTTABLE04",UtilHelper.getString(LOTTABLE04, null));
        mLOT.put("LOTTABLE05",UtilHelper.getString(LOTTABLE05, null));
        mLOT.put("LOTTABLE06",UtilHelper.getString(LOTTABLE06, " "));
        mLOT.put("LOTTABLE07",UtilHelper.getString(LOTTABLE07, " "));
        mLOT.put("LOTTABLE08",UtilHelper.getString(LOTTABLE08, " "));
        mLOT.put("LOTTABLE09",UtilHelper.getString(LOTTABLE09, " "));
        mLOT.put("LOTTABLE10",UtilHelper.getString(LOTTABLE10, " "));
        mLOT.put("LOTTABLE11",UtilHelper.getString(LOTTABLE11, null));
        mLOT.put("LOTTABLE12",UtilHelper.getString(LOTTABLE12, null));
        LegacyDBHelper.ExecInsert( "LOTATTRIBUTE", mLOT);
        return LOT;
    }


}
