package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.DaasProjectCode;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * dz
  --注册方法，插入到订单，没有订单头创建订单头，没有订单行创建订单行，返回订单行数量

 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHLpnCreateSOAdd'
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHLpnCreateSOAdd', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'LpnCreateSOAdd', 'TRUE', 'dz', 'dz',
 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERTYPE,SKU,LPNORSN,NETWGT,GROSSWGT,TAREWGT,UOM,ESIGNATUREKEY,PRINTER,PROJECTID','0.10','0');
 */
public class LpnCreateSOAdd extends LegacyBaseService {
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
        //返回的数据
        String orderDetailCount = "";
//        EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);

        String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
        String orderType = serviceDataHolder.getInputDataAsMap().getString("ORDERTYPE");
        String sku = serviceDataHolder.getInputDataAsMap().getString("SKU");
        String lpnOrSN = serviceDataHolder.getInputDataAsMap().getString("LPNORSN");
        String netWgt = serviceDataHolder.getInputDataAsMap().getString("NETWGT");
        String grossWgt = serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
        String tareWgt = serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
        String uom = serviceDataHolder.getInputDataAsMap().getString("UOM");
        String printer = serviceDataHolder.getInputDataAsMap().getString("PRINTER");
        String esignatureKey = serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
        String projectId = serviceDataHolder.getInputDataAsMap().getString("PROJECTID");

        String userId = EHContextHelper.getUser().getUsername();
        

        HashMap<String,String> lotxLocxIdHashMap;
        String sn;

        try{

            if(SKU.isSerialControl(sku) && !IDNotes.isBoxId(lpnOrSN)){

                lotxLocxIdHashMap = LotxLocxId.findBySkuAndSerialNum(sku,lpnOrSN);
                sn =lpnOrSN;

            }else {
                lotxLocxIdHashMap = LotxLocxId.findById( lpnOrSN, true);
                sn = "";
            }

            if(UtilHelper.equals(lotxLocxIdHashMap.get("LOC"),"PICKTO")) ExceptionHelper.throwRfFulfillLogicException("货品目前已位于发货月台PICKTO库位,请移至存储库位后再进行拣货");
            OrderValidationHelper.checkOrderTypeAndQualityStatusByLPN(orderType,lotxLocxIdHashMap.get("ID"));
            //checkLpnExistInSO( lotxLocxIdHashMap.get("ID"));

            String packKey = lotxLocxIdHashMap.get("LOTTABLE01");
            BigDecimal netWgtBigDecimal =new BigDecimal(netWgt);
            BigDecimal grossWgtBigDecimal =new BigDecimal(grossWgt);
            BigDecimal tareWgtBigDecimal =new BigDecimal(tareWgt);
            BigDecimal baseUomNetWgtBigDecimal= UOM.UOMQty2StdQty( packKey, uom,netWgtBigDecimal);
            BigDecimal baseUomGrossWgtBigDecimal= UOM.UOMQty2StdQty( packKey, uom,grossWgtBigDecimal);
            BigDecimal baseUomTareWgtBigDecimal= UOM.UOMQty2StdQty( packKey, uom,tareWgtBigDecimal);

            String storerKey = CDSysSet.getStorerKey();

            HashMap<String, String> orderHashMap = null;
            //通过传入的orderKey是否为空来判断是否需要创建订单头
            if(UtilHelper.isEmpty(orderKey)){
                orderHashMap = insertOrder( orderType,projectId);
            }else {
                orderHashMap = Orders.findByOrderKey(orderKey,true);
            }
            //插入订单行
            HashMap<String,String> orderLineHashMap = insertOrderDetail( storerKey,orderHashMap.get("ORDERKEY"), sku, lotxLocxIdHashMap.get("ID"),sn, baseUomNetWgtBigDecimal.toPlainString(),baseUomGrossWgtBigDecimal.toPlainString(),baseUomTareWgtBigDecimal.toPlainString(), packKey, uom);

            String[] snList;
            if(UtilHelper.isEmpty(sn)){
                snList = new String[]{};
            }else {
                snList = new String[]{lpnOrSN};
            }

            HashMap<String,String> result = PickUtil.doRandomPick( orderHashMap.get("ORDERKEY"),orderLineHashMap.get("ORDERLINENUMBER"), lotxLocxIdHashMap,"", grossWgt, tareWgt, netWgt, uom, BigDecimal.ZERO, snList,esignatureKey,printer);

            String toId = result.get("TOID");
            String printLabel = result.get("PRINT");
            ChangeOpenSnMarksHelper.changeOpenSnMarksBYLpn(sku,toId,lotxLocxIdHashMap.get("ID"));


            //写日志到UDTRN
            Udtrn udtrn = new Udtrn();
            udtrn.FROMTYPE = "扫描容器直接出库-添加明细";
            udtrn.FROMTABLENAME = "ORDERDETAIL";
            udtrn.FROMKEY=orderHashMap.get("ORDERKEY");
            udtrn.TITLE01 = "出库类型代码";
            udtrn.CONTENT01 = orderType;
            udtrn.TITLE02 = "出库单号";
            udtrn.CONTENT02 = orderKey;
            udtrn.TITLE03 = "容器号/唯一码/箱号";
            udtrn.CONTENT03 = lpnOrSN;
            udtrn.Insert(userId);

            orderDetailCount = DBHelper.getValue( "SELECT COUNT(ORDERKEY) FROM ORDERDETAIL WHERE ORDERKEY = ?",
                    new Object[]{orderHashMap.get("ORDERKEY")}, String.class, "");


            EXEDataObject theOutDO = new EXEDataObject();
//            theOutDO.clearDO();
//            theOutDO.setRow(theOutDO.createRow());

            theOutDO.setAttribValue("ORDERKEY",orderHashMap.get("ORDERKEY"));
            theOutDO.setAttribValue("EXTERNORDERKEY",orderHashMap.get("EXTERNORDERKEY"));
            theOutDO.setAttribValue("ORDERDETAILCOUNT",orderDetailCount);
            theOutDO.setAttribValue("TOID",toId);
            theOutDO.setAttribValue("PRINTLABEL",printLabel);

            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);
        }catch (Exception e){
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }
    }

    private HashMap<String,String> insertOrder( String orderType,String projectId) throws Exception {
        String storerKey = CDSysSet.getStorerKey();
        String userId = EHContextHelper.getUser().getUsername();
        String orderKey;
        orderKey = LegacyDBHelper.GetNCounterBill( "ORDER");
        HashMap<String,String> orders = new LinkedHashMap<>();
        orders.put("ADDWHO",userId);
        orders.put("EDITWHO",userId);
        orders.put("TYPE", orderType);
        orders.put("OHTYPE", orderType);
        orders.put("STATUS","06");
        orders.put("ORDERKEY",orderKey);
        orders.put("EXTERNORDERKEY","WMS"+orderKey);
        orders.put("STORERKEY", storerKey);
        if(!UtilHelper.isEmpty(projectId)){
            Map<String, String> daasProjectCode = DaasProjectCode.getByProjectId( projectId);
            orders.put("NOTES",projectId);
            orders.put("CLIENRPROJECTCODE",daasProjectCode.get("CLIENRPROJECTCODE"));
        }
        LegacyDBHelper.ExecInsert("orders",orders);

        //写日志到UDTRN
        Udtrn udtrn = new Udtrn();
        udtrn.FROMTYPE = "扫描容器直接出库-创建订单";
        udtrn.FROMTABLENAME = "ORDERS";
        udtrn.FROMKEY=orderKey;
        udtrn.TITLE01 = "出库类型代码";
        udtrn.CONTENT01 = orderType;
        udtrn.TITLE02 = "出库单号";
        udtrn.CONTENT02 = orderKey;
        udtrn.Insert(userId);
        return orders;
    }

    private HashMap<String, String> insertOrderDetail( String storerKey, String orderKey, String sku, String lpn, String sn, String netwgt, String grossWgt, String tareWgt, String packKey, String uom) throws Exception {

        String userId = EHContextHelper.getUser().getUsername();

        String orderLineNumber = DBHelper.getValue( "select max(orderlinenumber) from orderdetail where orderkey=?"
                ,new String[]{orderKey}, "0");
        orderLineNumber=Integer.toString(Integer.parseInt(orderLineNumber)+1);
        while (orderLineNumber.length()<5) orderLineNumber="0"+orderLineNumber;

        HashMap<String,String> orderDetail = new LinkedHashMap<>();
        orderDetail.put("ADDWHO", userId);
        orderDetail.put("EDITWHO", userId);
        orderDetail.put("STATUS","02");
        orderDetail.put("ORDERKEY", orderKey);
        orderDetail.put("ORDERLINENUMBER",orderLineNumber);
        orderDetail.put("externorderkey","WMS"+ orderKey);
        orderDetail.put("STORERKEY", storerKey);
        orderDetail.put("SKU", sku);
        orderDetail.put("ORIGINALQTY", netwgt);
        orderDetail.put("OPENQTY",  netwgt);
        orderDetail.put("GROSSWGTEXPECTED",  grossWgt);
        orderDetail.put("TAREWGTEXPECTED",  tareWgt);
        orderDetail.put("PACKKEY", packKey);
        orderDetail.put("UOM", uom);
        orderDetail.put("IDREQUIRED", lpn);
        orderDetail.put("SERIALNUMBER", sn);
        orderDetail.put("NEWALLOCATIONSTRATEGY", "N21"); //分配策略:匹配数量，然后最佳适配

        LegacyDBHelper.ExecInsert( "ORDERDETAIL", orderDetail);

        return orderDetail;

    }



    private void checkLpnExistInSO( String lpn) throws Exception {

        String sql = "select ORDERKEY from ORDERDETAIL where IDREQUIRED = ? AND STATUS in ('02','04','06','09') ";

        List<HashMap<String,String>> orderList = DBHelper.executeQuery( sql, new Object[]{lpn});

        if(orderList.size()>0) {
           throw new Exception("此容器已经存在出库单"+orderList.get(0).get("ORDERKEY")+"中");
        }
    }
}
