package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.DemandAllocation;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHDynamicPickConfirm';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHDynamicPickConfirm', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'DynamicPickConfirm','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,DEMANDKEY,FROMID,GROSSWGT,TAREWGT,NETWGT,UOM,PRINTER,ESIGNATUREKEY','0.10','0');

 **/

public class DynamicPickConfirm extends LegacyBaseService {

    private static final long serialVersionUID = 1L;


    private String printer = null;
    private String esignatureKey= null;

    public DynamicPickConfirm()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {


        try
        {


            String demandKey =   serviceDataHolder.getInputDataAsMap().getString("DEMANDKEY");
            String fromId =   serviceDataHolder.getInputDataAsMap().getString("FROMID");
            String grossWgt =  serviceDataHolder.getInputDataAsMap().getString( "GROSSWGT");
            String tareWgt =   serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
            String netWgt =   serviceDataHolder.getInputDataAsMap().getString( "NETWGT");
            String uom =   serviceDataHolder.getInputDataAsMap().getString( "UOM");
            printer =   serviceDataHolder.getInputDataAsMap().getString( "PRINTER");
            esignatureKey=  serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");


            if (UtilHelper.isEmpty(demandKey)) ExceptionHelper.throwRfFulfillLogicException("需求明细号不能为空");

            //context.theSQLMgr.transactionBegin();


            HashMap<String,String> daHashMap = DemandAllocation.findByKey( demandKey,true);

            String orderKey =daHashMap.get("ORDERKEY");
            String orderLineNumber =daHashMap.get("ORDERLINENUMBER");
            String sku = daHashMap.get("SKU");

            HashMap<String,String> lotxLocxIdHashMap;
            List<String> snList = new ArrayList<>();

            if(SKU.isSerialControl(sku) && !IDNotes.isBoxId(fromId)){

                lotxLocxIdHashMap = LotxLocxId.findBySkuAndSerialNum(sku,fromId);
                snList.add(fromId);

            }else {
                lotxLocxIdHashMap = LotxLocxId.findById( fromId, true);
            }
            
            dynamicPickCheck(demandKey,lotxLocxIdHashMap,netWgt,uom);
            decreaseDemandAllocation( demandKey,lotxLocxIdHashMap,netWgt,uom);
            HashMap<String,String> result = PickUtil.doRandomPick( orderKey,orderLineNumber, lotxLocxIdHashMap,"", grossWgt, tareWgt, netWgt, uom, BigDecimal.ZERO, snList.stream().toArray(String[]::new),esignatureKey,printer);
            String toId = result.get("TOID");
            String printLabel = result.get("PRINT");


            //context.theSQLMgr.transactionCommit();

            serviceDataHolder.getInputDataAsMap().setAttribValue("toid", toId);
            serviceDataHolder.getInputDataAsMap().setAttribValue("PRINTLABEL",printLabel);
            serviceDataHolder.setOutputData(serviceDataHolder.getInputDataAsMap());
        }
        catch (Exception e)
        {
//            context.transactionAbort();
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());
        }finally {
            
        }

    }

    private void dynamicPickCheck( String demandKey,HashMap<String, String> lotxLocxIdHashMap, String netWgt, String uom) throws Exception {

        HashMap<String,String> daHashMap = DemandAllocation.findByKey( demandKey,true);

        checkIfLotMatchDemand( daHashMap, lotxLocxIdHashMap);

        String daQtyAllocated = daHashMap.get("QTYALLOCATED");
        BigDecimal uomQtyTobePicked = UtilHelper.str2Decimal(netWgt,"净重",false);

        BigDecimal stdQtyTobePicked = UOM.UOMQty2StdQty( lotxLocxIdHashMap.get("PACKKEY"), uom, uomQtyTobePicked);

        if(UtilHelper.decimalStrCompare(stdQtyTobePicked.toPlainString(),daQtyAllocated)>0){

            ExceptionHelper.throwRfFulfillLogicException("实际拣货量"+stdQtyTobePicked+"大于需求分配量"+daQtyAllocated);

        }


    }



    private void checkIfLotMatchDemand( HashMap<String,String> daHashMap, HashMap<String, String> lotxLocxIdHashMap) {

        String daSku = daHashMap.get("SKU");

        String daLottable01 = daHashMap.get("LOTTABLE01");
        String daELottable09 = daHashMap.get("ELOTTABLE09");
        String daELottable03 = daHashMap.get("ELOTTABLE03");


        String disLpn = !UtilHelper.isEmpty(lotxLocxIdHashMap.get("SERIALNUMBER")) ? lotxLocxIdHashMap.get("SERIALNUMBER") :lotxLocxIdHashMap.get("ID");

        if(!UtilHelper.equals(daSku, lotxLocxIdHashMap.get("SKU"))){

            ExceptionHelper.throwRfFulfillLogicException("订单出库物料代码应为"+ daSku +",实际拣货容器"+disLpn+"的物料代码为"+ lotxLocxIdHashMap.get("SKU")+",不允许拣货");

        }

        if(!UtilHelper.equals(daELottable09, lotxLocxIdHashMap.get("ELOTTABLE09"))){

            ExceptionHelper.throwRfFulfillLogicException("订单出库批次应为"+ daELottable09 +",实际拣货容器"+disLpn+"的批次为"+ lotxLocxIdHashMap.get("ELOTTABLE09")+",不允许拣货");

        }

        if(!UtilHelper.equals(daLottable01, lotxLocxIdHashMap.get("LOTTABLE01"))){

            ExceptionHelper.throwRfFulfillLogicException("订单出库包装应为"+ daLottable01 +",实际拣货容器"+disLpn+"的包装为"+ lotxLocxIdHashMap.get("LOTTABLE01")+",不允许拣货");

        }

//            //根据ID和LOT的质量状态计算该容器实际所属的质量状态，这个条件成立的前提是业务上要能够保证质量状态计算后不会存在相互交叠的情况。如：不能存在特批放行能够出放行的货、并且放行也能出特批放行的货。
//            //否则需要先通过DEMANDALLOCATION算出质量要求对应的LOT QSTATUS + ID QSTATUS的组合后去匹配拣货LPN的批属性。
//            String idQualityStatus = PickUtil.computeQualityStatus(lotxLocxIdHashMap.get("ELOTTABLE03"),lotxLocxIdHashMap.get("QUALITYSTATUS"));

        if(!UtilHelper.equals(daELottable03, lotxLocxIdHashMap.get("ELOTTABLE03"))){

            String daELottable03Desc = CodeLookup.getCodeLookupValue("MQSTATUS", daELottable03,"DESCRIPTION","质量状态");
            String idQualityStatusDesc = CodeLookup.getCodeLookupValue("MQSTATUS", lotxLocxIdHashMap.get("ELOTTABLE03"),"DESCRIPTION","质量状态");
            ExceptionHelper.throwRfFulfillLogicException("订单出库质量状态应为"+daELottable03Desc+",实际拣货容器"+disLpn+"的质量状态为"+idQualityStatusDesc+",不允许拣货");

        }
    }

    private void decreaseDemandAllocation( String demandKey, HashMap<String,String> lotxLocxIdHashMap, String uomNetWgt,String uom) throws Exception {

        Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());

        HashMap daHashMap = DemandAllocation.findByKey(demandKey,true);

        BigDecimal uomQtyTobePicked = UtilHelper.str2Decimal(uomNetWgt,"净重",false);
        BigDecimal stdQtyTobePicked = UOM.UOMQty2StdQty( lotxLocxIdHashMap.get("PACKKEY"), uom, uomQtyTobePicked);


        if(UtilHelper.decimalStrCompare(daHashMap.get("QTYALLOCATED").toString(),stdQtyTobePicked.toPlainString()) < 0){
            ExceptionHelper.throwRfFulfillLogicException("拣货数量"+stdQtyTobePicked.toPlainString()+"不允许大于需求分配量"+daHashMap.get("QTYALLOCATED").toString());
        }else if(UtilHelper.decimalStrCompare(daHashMap.get("QTYALLOCATED").toString(),stdQtyTobePicked.toPlainString()) == 0){
            DBHelper.executeUpdate(
                    "DELETE DEMANDALLOCATION WHERE DEMANDKEY = ? ",
                    new Object[]{  demandKey});
        }else{
            //QTYALLOCATED>stdQtyTobePicked
            DBHelper.executeUpdate(
                    "UPDATE DEMANDALLOCATION SET QTYALLOCATED = QTYALLOCATED - ?, EditDate = ?, EditWho = ? WHERE DEMANDKEY = ? ",
                    new Object[]{
                            stdQtyTobePicked.toPlainString(),
                            currentDate,
                            EHContextHelper.getUser().getUsername(),
                            demandKey
                    });
        }

        DBHelper.executeUpdate(
                "UPDATE OrderDetail SET QtyAllocated = QtyAllocated - ?, EditDate = ?, EditWho = ? WHERE Orderkey = ? AND OrderLineNumber = ? ",
                new Object[]{
                stdQtyTobePicked.toPlainString(),
                currentDate,
                EHContextHelper.getUser().getUsername(),
                daHashMap.get("ORDERKEY").toString(),
                daHashMap.get("ORDERLINENUMBER").toString()
        });
    }


}