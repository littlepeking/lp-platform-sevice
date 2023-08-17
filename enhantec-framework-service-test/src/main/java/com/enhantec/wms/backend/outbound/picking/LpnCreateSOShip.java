package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.outbound.OutboundUtils;
import com.enhantec.wms.backend.outbound.allocation.OrderProcessingP1S1;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;

import java.util.*;
import java.util.Map;

/**
 * dz 20210524
 * 插入的是SCPRDMST表
   -- 注册方法
    DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHLpnCreateSOShip'
    insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
    values ('EHLpnCreateSOShip', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'LpnCreateSOShip', 'TRUE', 'dz', 'dz',
    'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,PRINTER,ESIGNATUREKEY','0.10','0');
 */
@Deprecated // BY JOHN
public class LpnCreateSOShip extends WMSBaseService {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
//        EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);

        String userId = EHContextHelper.getUser().getUsername();
        String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
        String printer = serviceDataHolder.getInputDataAsMap().getString("PRINTER");

        String esignatuerKey = "";
        try{
            Map<String, String> orderInfo = Orders.findByOrderKey( orderKey, true);
            if(LegecyUtilHelper.isNull(orderInfo)) throw new Exception("找不到发货订单："+orderKey);
            if(orderInfo.get("STATUS").equals("95")){
                ExceptionHelper.throwRfFulfillLogicException("不能发运重复订单");
            }

            List<Map<String,String>> orderDetails = DBHelper.executeQuery(
                    "select ORDERLINENUMBER,STORERKEY,SKU,IDREQUIRED,LOTTABLE06,SUSR1,SUSR3,STATUS " +
                            ",ORIGINALQTY,OPENQTY,GROSSWGTEXPECTED,TAREWGTEXPECTED, SHIPPEDQTY,QTYPREALLOCATED,QTYALLOCATED,QTYPICKED,UOM,PACKKEY " +
                            "from orderdetail where orderkey=? and STATUS <>'95' order by orderlinenumber",
                    new String[]{orderKey});
            for (Map<String,String> odHashMap : orderDetails) {
                String id = odHashMap.get("IDREQUIRED");
                String netWgt = odHashMap.get("ORIGINALQTY");
                String grossWgt = odHashMap.get("GROSSWGTEXPECTED");
                String tareWgt = odHashMap.get("TAREWGTEXPECTED");
                String uom = odHashMap.get("UOM");

                //String stdUom = UOM.getStdUOM( odHashMap.get("PACKKEY"));

                BigDecimal stdGrossWgtDecimal = UtilHelper.str2Decimal(grossWgt,"毛重",false);
                BigDecimal stdNetWgtDecimal = UtilHelper.str2Decimal(netWgt,"净重",false);
                BigDecimal stdTareWgtDecimal = UtilHelper.str2Decimal(tareWgt,"皮重",false);

                OutboundUtils.checkQtyIsAvailableInIDNotes(id,
                        UtilHelper.str2Decimal(netWgt,"出库量",false));
                OutboundUtils.checkQtyIsAvailableInLotxLocxId(id,
                        UtilHelper.str2Decimal(netWgt,"出库量",false), BigDecimal.ZERO);

                String idToBeShipped =  null;

                Map<String,String> originalIdHashMap = LotxLocxId.getAvailInvById(id);

                //容器数量>拣货量并且拣货量>0，打印剩余量标签
                if(UtilHelper.decimalStrCompare(originalIdHashMap.get("QTY"), netWgt)>0
                        && UtilHelper.decimalStrCompare(netWgt, "0")>0) {
                    PrintHelper.printLPNByIDNotes(id, Labels.LPN_UI_SY, printer, "1", "拣货剩余量标签");

                    //插入拣货到容器的IDNOTES信息
                    idToBeShipped = IDNotes.splitWgtById( stdGrossWgtDecimal, stdNetWgtDecimal, stdTareWgtDecimal, grossWgt, netWgt, tareWgt, uom, id, "", orderKey,false);

                    Map<String,String> fieldsToBeUpdate = new HashMap<>();
                    fieldsToBeUpdate.put("LASTSHIPPEDLOC", originalIdHashMap.get("LOC")); //该ID最后一次的拣货自库位
//                    fieldsToBeUpdate.put("LASTLOC", originalIdHashMap.get("LOC")); //该ID的上一个库位
                    fieldsToBeUpdate.put("LASTID", originalIdHashMap.get("ID")); //该ID的上一个ID

                    IDNotes.update( idToBeShipped , fieldsToBeUpdate);

                    if(IDNotes.isLpnOrBoxId(idToBeShipped)) {
                        List<String> notPrintLpnLabelOrderTypes = CDSysSet.getNotPrintLpnLabelOrderTypes();
                        if(null == notPrintLpnLabelOrderTypes || !notPrintLpnLabelOrderTypes.contains(orderInfo.get("TYPE"))) {
                            PrintHelper.printLPNByIDNotes( idToBeShipped, Labels.LPN, printer, "1", "非整桶拣货标签");
                        }
                    }

                }else if(UtilHelper.decimalStrCompare(originalIdHashMap.get("QTY"), netWgt)==0){

                    idToBeShipped = id;

                    Map<String,String> fieldsToBeUpdate = new HashMap<String,String>();

                    fieldsToBeUpdate.put("GROSSWGTLABEL", grossWgt);//原毛重标签量
                    fieldsToBeUpdate.put("TAREWGTLABEL", tareWgt);//原皮重标签量
                    fieldsToBeUpdate.put("NETWGTLABEL", netWgt);//原净重标签量
                    fieldsToBeUpdate.put("UOMLABEL", uom);//采集读取的计量单位
                    fieldsToBeUpdate.put("LASTSHIPPEDLOC", originalIdHashMap.get("LOC")); //该ID最后一次的拣货自库位
//                    fieldsToBeUpdate.put("LASTLOC", originalIdHashMap.get("LOC")); //该ID的上一个库位
                    fieldsToBeUpdate.put("LASTID", originalIdHashMap.get("ID")); //该ID的上一个ID
                    IDNotes.update( idToBeShipped, fieldsToBeUpdate);
                }

                //对分拆出的容器扣IDNOTES库存
                final String idToBeShippedF = idToBeShipped;
                DBHelper.executeUpdate(
                        "UPDATE IDNOTES SET GROSSWGT = GROSSWGT-?, NETWGT = NETWGT-?,  EDITWHO = ?, EDITDATE = ? WHERE ID = ? ",
                        new ArrayList<Object>(){{
                            add(netWgt);
                            add(netWgt);
                            add(userId);
                            add(UtilHelper.getCurrentSqlDate());
                            add(idToBeShippedF);
                        }});
                //:TODO:校验发运的IDNOTES库存余额应为0并移至历史表，否则报错
                Map<String,String> shippedIdNotesHashMap = IDNotes.findById(id,true);
                IDNotes.archiveIDNotes( shippedIdNotesHashMap);


            }

            //扣系统库存
            allocateAndShip(orderKey);

            String status = DBHelper.getValue(
                    "select status from orders where orderkey = ? ", new Object[]{orderKey},String.class,"订单");

            if(!"95".equals(status)) ExceptionHelper.throwRfFulfillLogicException("出库失败，请检查库存是否可用!");

            //写日志
            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=esignatuerKey;
            UDTRN.FROMTYPE="直接扫描条码出库";
            UDTRN.FROMTABLENAME="ORDERS";
            UDTRN.FROMKEY=orderKey;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="出库单号";    UDTRN.CONTENT01=orderKey;
            UDTRN.Insert( userId);

        }catch (Exception e){
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }
        ServiceDataMap theOutDO = new ServiceDataMap();
//        theOutDO.clearDO();
//        theOutDO.setRow(theOutDO.createRow());


        serviceDataHolder.setReturnCode(1);
        serviceDataHolder.setOutputData(theOutDO);
//      

    }
    /**
     *

     * @param orderKey
     */
    private void allocateAndShip( String orderKey) {
        ServiceDataHolder serviceDataHolder = new ServiceDataHolder();


        ServiceDataMap allocateDO = new ServiceDataMap();
        allocateDO.setAttribValue("orderKey", orderKey);
        allocateDO.setAttribValue("osKey" , "");
        allocateDO.setAttribValue("doCarton" , "Y");
        allocateDO.setAttribValue("doRoute" , "N");
        allocateDO.setAttribValue("tblPrefix", "");
        allocateDO.setAttribValue("preallocateOnly" , "N");

        serviceDataHolder.setInputData(allocateDO);

        OrderProcessingP1S1 allocateProcess = new OrderProcessingP1S1();
        allocateProcess.execute(serviceDataHolder);
//        context.theEXEDataObjectStack.pop();

        ServiceDataMap shipDO = new ServiceDataMap();

        shipDO.setAttribValue("orderkey", orderKey);
        shipDO.setAttribValue("TransactionStarted" , "true");
//todo
//        context.theEXEDataObjectStack.push(shipDO);
//        Process shipProcess = context.searchObjectLibrary("NSPMASSSHIPORDERS"));
//        shipProcess.execute();
//        context.theEXEDataObjectStack.pop();
    }

    /**
     *

     * @param orderKey
     */
    private void ship( String orderKey) {

        ServiceDataMap shipDO = new ServiceDataMap();
        shipDO.setAttribValue("orderkey" , orderKey);
        shipDO.setAttribValue("TransactionStarted" , "true");

//        context.theEXEDataObjectStack.push(shipDO);
//        Process shipProcess = context.searchObjectLibrary("NSPMASSSHIPORDERS"));
//        shipProcess.execute();
//        context.theEXEDataObjectStack.pop();
    }
}
