package com.enhantec.wms.backend.outbound.utils;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.code.CDOrderType;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.UserInfo;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OrderValidationHelper {

    public static void checkIdQualityStatusMatchOrderType(Context context, String orderKey, HashMap<String,String> lotxLocxIdInfo) {


        HashMap<String, String> orderHashMap = Orders.findByOrderKey(context, orderKey, true);


        HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, "ORDERTYPE", orderHashMap.get("TYPE"));

        if (!UtilHelper.isEmpty(orderTypeEntry.get("UDF5"))) {

            String[] orderTypeStatuses = orderTypeEntry.get("UDF5").split(",");

            String orderTypeStatusesString = Arrays.stream(orderTypeStatuses).map(e ->
                    CodeLookup.getCodeLookupValue(context, "MQSTATUS", e, "DESCRIPTION", "质量状态")
            ).collect(Collectors.joining(" 或 "));

            if (!UtilHelper.isEmpty(lotxLocxIdInfo.get("ELOTTABLE03")) && !Arrays.stream(orderTypeStatuses).anyMatch(e-> e.trim().equals(lotxLocxIdInfo.get("ELOTTABLE03")))) {
                String statusTranslated = CodeLookup.getCodeLookupValue(context, "MQSTATUS", lotxLocxIdInfo.get("ELOTTABLE03"), "DESCRIPTION", "质量状态");
                ExceptionHelper.throwRfFulfillLogicException("订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许分配质量状态为 " + orderTypeStatusesString + " 的物料，但容器条码"
                        + lotxLocxIdInfo.get("ID")+"的质量状态为"+statusTranslated);
            }
        }
    }

    public static void checkOrderTypeAndQualityStatusMatch4Alloc(Context context, String orderKey) throws Exception{

        if (UtilHelper.isEmpty(orderKey)) ExceptionHelper.throwRfFulfillLogicException("待分配的订单号不能为空");

        HashMap<String, String> order = Orders.findByOrderKey(context, orderKey, true);

        List<HashMap<String, String>> orderDetails = Orders.findOrderDetailsByOrderKey(context, orderKey, true);


        for(HashMap<String, String> orderDetail: orderDetails) {

            checkOrderQualityStatus(context, order, orderDetail);

            checkOrderSerialNumber(context, orderDetail);

        }
    }

    private static void checkOrderQualityStatus(Context context, HashMap<String, String> order, HashMap<String, String> orderDetail) {
        HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, "ORDERTYPE", order.get("TYPE"));

        if (!UtilHelper.isEmpty(orderTypeEntry.get("UDF5"))) {


            String[] statuses = orderTypeEntry.get("UDF5").split(",");

            String statusesStr = Arrays.stream(statuses).map(e ->
                    CodeLookup.getCodeLookupValue(context, "MQSTATUS", e, "DESCRIPTION", "质量状态")
            ).collect(Collectors.joining(" 或 "));


            if (!UtilHelper.isEmpty(orderDetail.get("ELOTTABLE03")) && !Arrays.stream(statuses).anyMatch(e-> e.trim().equals(orderDetail.get("ELOTTABLE03")))) {
                String statusTranslated = CodeLookup.getCodeLookupValue(context, "MQSTATUS", orderDetail.get("ELOTTABLE03"), "DESCRIPTION", "质量状态");
                ExceptionHelper.throwRfFulfillLogicException("订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许分配质量状态为 " + statusesStr + " 的物料，但订单行"
                        + orderDetail.get("ORDERLINENUMBER")+"的质量状态为"+statusTranslated);
            }
        }
    }

    private static void checkOrderSerialNumber(Context context, HashMap<String, String> orderDetail) throws Exception {
        if(SKU.isSerialControl(context, orderDetail.get("SKU")) ){

            if(UtilHelper.isEmpty(orderDetail.get("SERAILNUMBER"))) {
                if (!UtilHelper.isInteger(orderDetail.get("OPENQTY")))
                    ExceptionHelper.throwRfFulfillLogicException(orderDetail.get("SKU") + "为唯一码管理物料，出库数量必须为正整数");
            }else{
                if(BigDecimal.ONE.compareTo(new BigDecimal(orderDetail.get("QTY")))!=0)
                    ExceptionHelper.throwRfFulfillLogicException(orderDetail.get("SKU") + "为唯一码管理物料，当指定唯一码出库时，出库数量必须为1");
            }
        }
    }
//
//    public static void checkOrderTypeAndQualityStatusMatch4Ship(Context context, String orderKey) throws Exception{
//
//        if (UtilHelper.isEmpty(orderKey)) ExceptionHelper.throwRfFulfillLogicException("待分配的订单号不能为空");
//
//        HashMap<String, String> order = Orders.findByOrderKey(context, orderKey, true);
//
//        HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, "ORDERTYPE", order.get("TYPE"));
//
//        if (!UtilHelper.isEmpty(orderTypeEntry.get("UDF5"))) {
//
//            List<HashMap<String, String>> pickDetails = PickDetail.findByOrderKey(context, orderKey, false);
//
//            if(pickDetails.size()!=0) {
//                for (HashMap<String, String> pickDetail : pickDetails) {
//                    if(!pickDetail.get("STATUS").equals("9")) {
//                        HashMap<String, Object> la = VLotAttribute.findByLot(context, pickDetail.get("LOT"), true);
//
//                        if (!UtilHelper.equals(String.valueOf(la.get("ELOTTABLE03")), orderTypeEntry.get("UDF5")))
//                            ExceptionHelper.throwRfFulfillLogicException("订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许发运质量状态为" + orderTypeEntry.get("UDF5") + "的物料");
//                    }
//                }
//            }
//        }
//    }

    public static void checkOrderTypeAndQualityStatusByPickDetailKey(Context context, String pickDetailKey) throws Exception{

        if (UtilHelper.isEmpty(pickDetailKey)) ExceptionHelper.throwRfFulfillLogicException("拣货明细号不能为空");

        HashMap<String, String> pickDetail = PickDetail.findByPickDetailKey(context, pickDetailKey, true);

        String orderKey = pickDetail.get("ORDERKEY");

        HashMap<String, String> order = Orders.findByOrderKey(context, orderKey, true);

        HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, "ORDERTYPE", order.get("TYPE"));

        if (!UtilHelper.isEmpty(orderTypeEntry.get("UDF5"))) {

            HashMap<String, Object> la = VLotAttribute.findByLot(context, pickDetail.get("LOT"),true);
//
//                if (!UtilHelper.equals(String.valueOf(la.get("ELOTTABLE03")), orderTypeEntry.get("UDF5")))
//                    ExceptionHelper.throwRfFulfillLogicException("订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许发运质量状态为" + orderTypeEntry.get("UDF5") + "的物料");

            String[] statuses = orderTypeEntry.get("UDF5").split(",");

            String statusesStr = Arrays.stream(statuses).map(e ->
                    CodeLookup.getCodeLookupValue(context, "MQSTATUS", e, "DESCRIPTION", "质量状态")
            ).collect(Collectors.joining(" 或 "));

            if (!Arrays.stream(statuses).anyMatch(e-> e.trim().equals(la.get("ELOTTABLE03")))) {
                String statusTranslated = CodeLookup.getCodeLookupValue(context, "MQSTATUS", la.get("ELOTTABLE03").toString(), "DESCRIPTION", "质量状态");
                ExceptionHelper.throwRfFulfillLogicException(
                        "订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许发运质量状态为 " + statusesStr + " 的物料，" +
                        "但当前待发运的货品质量状态为"+statusTranslated);
            }
        }
    }


    public static void checkOrderTypeAndQualityStatusByLPN(Context context, String orderType, String lpn) throws Exception{

        if (UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("容器条码不能为空");

        HashMap<String, String> lotxLocxIdInfo = LotxLocxId.findById(context, lpn, true);

        HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, "ORDERTYPE", orderType);

        if (!UtilHelper.isEmpty(orderTypeEntry.get("UDF5"))) {

            String[] statuses = orderTypeEntry.get("UDF5").split(",");

            String statusesStr = Arrays.stream(statuses).map(e ->
                    CodeLookup.getCodeLookupValue(context, "MQSTATUS", e, "DESCRIPTION", "质量状态")
            ).collect(Collectors.joining(" 或 "));

            if (!Arrays.stream(statuses).anyMatch(e-> e.trim().equals(lotxLocxIdInfo.get("ELOTTABLE03")))) {
                ExceptionHelper.throwRfFulfillLogicException("订单类型" + orderTypeEntry.get("DESCRIPTION") + "只允许使用质量状态为 " + statusesStr + " 的物料");
            }


        }
    }

    public static boolean checkIfSkuNeedAllocate(UserInfo userInfo, String orderKey, String sku) {

//        HashMap<String,String> repackNotAllocateHashMap = DBHelper.getRecord(userInfo,
//                "select UDF1 from codelkup where listname='SYSSET' and code='PACKNALLOC'",
//                new Object[]{},"分装SKU的出库单不允许进行分配",false);
//
//        if(repackNotAllocateHashMap==null || !"Y".equalsIgnoreCase(repackNotAllocateHashMap.get("UDF1")))
//            return true;


        HashMap<String,String> repackOrdTypeHashMap = DBHelper.getRecord(userInfo,
                "select UDF1 from codelkup where listname='SYSSET' and code='REPACKORDT'",
                new Object[]{},"分装出库单类型",false);

        String repackOrderType = "";
        if(repackOrdTypeHashMap!=null) {
            repackOrderType = repackOrdTypeHashMap.get("UDF1");
        }

        String orderType = DBHelper.getValue(userInfo,
                "SELECT TYPE FROM ORDERS WHERE ORDERKEY = ? ",
                new Object[]{orderKey},"订单类型");


        //20211006 改为订单类型和物料的固液类型同时都满足条件时才不进行分配。--john
        //if (!ORDER_TYPE_SAMPLE.equals(orderType) && !repackOrderType.equals(orderType)) {
        if(CDOrderType.isRepackOrderType(userInfo,orderType) && isRepackSku(userInfo,sku)) return false;

        return true;

    }

    public static boolean isRepackSku(UserInfo userInfo, String sku){

        String isRepackSku = DBHelper.getValue(userInfo,
                "SELECT CODELKUP.UDF1 FROM SKU,CODELKUP WHERE CODELKUP.LISTNAME ='SOLIDLIQUI' AND SKU.ITEMCHARACTERISTIC2 = CODELKUP.CODE AND SKU = ? ",
                new Object[]{sku},"是否分装物料标志",false);

        return !UtilHelper.isEmpty(isRepackSku) && "Y".equalsIgnoreCase(isRepackSku);

    }

    public static boolean isRepackOrderType(Context context, String orderKey) {

        

        String repackOrderType = DBHelper.getValue(context, "select udf1 from codelkup where listname='SYSSET' and code='REPACKORDT'"
        ,new Object[]{},"分装出库单类型代码未设置").toString();

        String orderType = DBHelper.getValue(context, "select TYPE from ORDERS where ORDERKEY = ? "
                ,new Object[]{orderKey},"订单类型").toString();

        return orderType.equals(repackOrderType);

    }

        public static void validateFieldsBeforeShip(Context context, String orderKey ){
            HashMap<String, String> order = Orders.findByOrderKey(context, orderKey, true);
        String mustInputWidget=CodeLookup.getCodeLookupValue(context,"ORDERTYPE",order.get("TYPE"),"UDF10","必输字段");
        if (!UtilHelper.isEmpty(mustInputWidget)){
            String[] mustInputWidgetArray;
            if (mustInputWidget.indexOf(":")>0){
                 mustInputWidgetArray=mustInputWidget.substring(0,mustInputWidget.indexOf(":")).split(";");
            }else {
                 mustInputWidgetArray=mustInputWidget.split(";");
            }
            if (mustInputWidgetArray.length>0){
                ArrayList<String> errMsg=new ArrayList<>();
                for (String s : mustInputWidgetArray) {
                    String[] arr=s.split("\\|");
                    if (arr.length==3&&"9".equals(arr[2])){
                        if (UtilHelper.isEmpty(order.get(UtilHelper.trim(arr[0])))) {
                            errMsg.add(arr[1]);
                        }
                    }

                }
                if (!errMsg.isEmpty()){
                    ExceptionHelper.throwRfFulfillLogicException("出库单字段" + errMsg.toString() + "为空请填写后再进行发运");
                }
            }
        }

    }
    public static void validateReturnPo(Context context, String orderKey ) throws Exception {
        HashMap<String, String> orderInfo = Orders.findByOrderKey(context, orderKey, true);
        //生基采购退货
        String buyerPo=orderInfo.get("BUYERPO");
        String buyerPoLine=orderInfo.get("BUYERPOLINE");
        String returnReceiptkey=orderInfo.get("RETURNRECEIPTKEY");
        //因一个采购退货出库单对应一个采购入库 一个采购入库只会有一个批次
        String SQL="select DISTINCT lottable06 from orderdetail where orderkey = ?";
        List<HashMap<String,String>> list= DBHelper.executeQuery(context, SQL, new Object[]{ orderKey});
        if (list.size()>1) throw new Exception("出库单明细内指定入厂批次不唯一，无法出库");
        String lottable06= list.get(0).get("LOTTABLE06");
        String checkPo="select p.FROMKEY from RECEIPT r ,PRERECEIPTCHECK p where r.EXTERNRECEIPTKEY=p.RECEIPTLOT and r.receiptkey =? and p.FROMKEY = ? and p.FROMLINENO = ? ";
        HashMap<String,String> checkPorecord= DBHelper.getRecord(context, checkPo, new Object[]{ returnReceiptkey,buyerPo,buyerPoLine},"订单");
        if (checkPorecord == null)throw new Exception("录入的采购单号与入库单号不匹配");
        String checkreceipt="select receiptkey from RECEIPT r  where r.EXTERNRECEIPTKEY=? and r.receiptkey =? ";
        HashMap<String,String> checkreceiptrecord= DBHelper.getRecord(context, checkreceipt, new Object[]{ lottable06,returnReceiptkey},"订单");
        if (checkreceiptrecord == null)throw new Exception("录入的入厂批次与入库单号不匹配");
        String checkReceiptSku="select SKU from orderdetail where orderkey = ? and SKU  in (select DISTINCT SKU from RECEIPTDETAIL where receiptkey = ?)";
        List<HashMap<String,String>> checkReceiptSkuList = DBHelper.executeQuery(context,checkReceiptSku,new Object[]{orderKey,returnReceiptkey});
        if (checkReceiptSkuList.isEmpty()) throw new Exception("录入的物料与入库单物料不匹配");
        String checkLotWgt="select * from whlot l  where l.lot=? and l.Qty < (select cast(SUM(OPENQTY) as float) AS QTY from ORDERDETAIL where orderkey = ? group by ORDERKEY) ";
        HashMap<String,String> checkLotWgtRecord= DBHelper.getRecord(context, checkLotWgt, new Object[]{ lottable06,orderKey},"订单");
        if (checkLotWgtRecord != null)throw new Exception("录入的数量不允许超过批次库存量");

    }

}
