package com.enhantec.wms.backend.inbound.asn.utils;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.LotxId;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.SerialInventory;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;

import java.util.*;
import java.util.stream.Collectors;

public class ReceiptValidationHelper {


    /**
     * 收货单校验（本校验只校验单据，不校验库存。考虑性能优化，库存校验只在收货时进行）


     * @param receiptKey
     * @throws Exception
     */
    public static void validateASN( String receiptKey) throws Exception {

        Map<String, String> receiptInfo =  Receipt.findByReceiptKey( receiptKey,true);
        List<Map<String, String>> receiptDetails =  Receipt.findReceiptDetails( receiptKey,true);

        HashSet<String> existingLpnHashSet = new HashSet<>();
        Map<String,String> receiptLotHashMap = new HashMap<>();
        Map<String,String> receiptQAStatusHashMap = new HashMap<>();
        Map<String,String> receiptInvTypeHashMap = new HashMap<>();
        Map<String,String> receiptSpecHashMap = new HashMap<>();
        Map<String,String> supplierCodeHashMap = new HashMap<>();
        Map<String,String> supplierLotHashMap = new HashMap<>();
        Map<String,String> elottable02LotHashMap = new HashMap<>();
        Map<String,String> lottable04HashMap = new HashMap<>();
        Map<String,String> purchaseCodeHashMap = new HashMap<>();

        String receivedDate = null;

        checkMustInputWidget(receiptInfo);

        for(Map<String,String> receiptDetail :receiptDetails){

            Map<String, String> receiptTypeInfo = CodeLookup.getCodeLookupByKey("RECEIPTYPE",receiptInfo.get("TYPE"));

            if(UtilHelper.isEmpty(receiptDetail.get("TOID"))) {

                //因为退货指令行退货的批次无法确认，只有收货时才能知道，因此不对其进行任何校验。
                if((Const.RECEIPT_RF_TYPE_RETURN_WITH_ASN.equalsIgnoreCase(receiptTypeInfo.get("UDF5")))) {
                    continue;
                }
                //RECEIPT_RF_TYPE_WITH_ASN指令行的容器条码允许为空，但需要校验其他信息。
                if(!(Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receiptTypeInfo.get("UDF5"))
                    && UtilHelper.isEmpty(receiptDetail.get("ORIGINALLINENUMBER")))){
                    //非指令行TOID不允许为空
                    ExceptionHelper.throwRfFulfillLogicException("收货明细行" + receiptDetail.get("RECEIPTLINENUMBER") + "的容器号不允许为空");
                }
                /**
                 * 如果配置了不允许超收，收货行的预期量+实收量必须<=指令行的预期量
                 */
                if(!CDReceiptType.enableExcessReceiving(receiptInfo.get("TYPE"))){
                    String originalQtyExpected = receiptDetail.get("QTYEXPECTED");
                    BigDecimal qtyExpectedAndReceived = new BigDecimal("0");
                    for (Map<String, String> detail : receiptDetails) {
                        if(!UtilHelper.isEmpty(detail.get("ORIGINALLINENUMBER")) && detail.get("ORIGINALLINENUMBER").equals(receiptDetail.get("RECEIPTLINENUMBER"))){
                            qtyExpectedAndReceived = qtyExpectedAndReceived.add(new BigDecimal(detail.get("QTYEXPECTED")));
                            qtyExpectedAndReceived = qtyExpectedAndReceived.add(new BigDecimal(detail.get("QTYRECEIVED")));
                        }
                    }
                    if(qtyExpectedAndReceived.compareTo(new BigDecimal(originalQtyExpected)) > 0){
                        ExceptionHelper.throwRfFulfillLogicException("指令行"+receiptDetail.get("RECEIPTLINENUMBER")+"的收货明细行总量大于预期，不允许收货");
                    }
                }


            } else {
                    //正常有ID的收货明细行容器条码不应该重复并且需要保证存在桶号和总桶号，桶号信息需自动生成，不允许界面录入
                    if (existingLpnHashSet.contains(receiptDetail.get("TOID")))
                        ExceptionHelper.throwRfFulfillLogicException("收货单中存在重复的容器条码" + receiptDetail.get("TOID"));
                    else {
                        //这里不对整单校验库存，在收货中进行校验，否则收货时会报错。
//                    Map<String,String> record= LotxLocxId.findById(receiptDetail.get("TOID"),false);
//                    if(record!=null)  ExceptionHelper.throwRfFulfillLogicException("库存中已在重复的容器条码"+receiptDetail.get("TOID"));
                        existingLpnHashSet.add(receiptDetail.get("TOID"));
                    }

                    if (UtilHelper.isEmpty(receiptDetail.get("BARRELNUMBER")))
                        ExceptionHelper.throwRfFulfillLogicException("收货明细行" + receiptDetail.get("RECEIPTLINENUMBER") + "的桶号不允许为空");
                    if (UtilHelper.isEmpty(receiptDetail.get("TOTALBARRELNUMBER")))
                        ExceptionHelper.throwRfFulfillLogicException("收货明细行" + receiptDetail.get("RECEIPTLINENUMBER") + "的总桶号不允许为空");
            }

            //带指令行的收货，自动收货的情况，预期量会自动置为0，要过滤掉这部分数据
            if(!"9".equals(receiptDetail.get("STATUS"))) {

            BigDecimal GROSSWGTEXPECTED = UtilHelper.str2Decimal(receiptDetail.get("GROSSWGTEXPECTED"),"收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"中的预期毛重/数量",false);
            BigDecimal TAREWGTEXPECTED = UtilHelper.str2Decimal(receiptDetail.get("TAREWGTEXPECTED"),"收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"中的预期皮重/数量",false);
            BigDecimal QTYEXPECTED = UtilHelper.str2Decimal(receiptDetail.get("QTYEXPECTED"),"收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"中的预期净重/数量",false);

            if(GROSSWGTEXPECTED.compareTo(BigDecimal.ZERO)<0)
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的毛重不能小于0");
            if(GROSSWGTEXPECTED.compareTo(BigDecimal.ZERO)<0)
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的毛重不能小于0");
            if(TAREWGTEXPECTED.compareTo(BigDecimal.ZERO)<0)
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的皮重不能小于0");
            if(QTYEXPECTED.compareTo(BigDecimal.ZERO)<0)
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的净重不能小于0");

                if (GROSSWGTEXPECTED.subtract(TAREWGTEXPECTED).compareTo(QTYEXPECTED) != 0)
                    ExceptionHelper.throwRfFulfillLogicException("收货行" + receiptDetail.get("RECEIPTLINENUMBER") + "的毛皮净重不匹配");
            }



            if(UtilHelper.isEmpty(receiptDetail.get("SKU")))
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的物料代码不允许为空");

            //check BUSR4 物料类型
            List<Map<String,String>> skuLotConfList = CodeLookup.getCodeLookupList("SKULOTCONF");

            Map<String,String> skuHashMap = SKU.findById(receiptDetail.get("SKU"),true);

            if(skuLotConfList!=null && skuLotConfList.size()>0) {

                skuLotConfList = skuLotConfList.stream().filter(
                        e -> UtilHelper.equals(e.get("UDF1"), skuHashMap.get("BUSR4"))).collect(Collectors.toList());

                checkReceiptMandatoryFields(receiptDetail, skuLotConfList);

            }

            if(!Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receiptTypeInfo.get("UDF5"))
               && !Const.RECEIPT_RF_TYPE_RETURN_WITH_ASN.equalsIgnoreCase(receiptTypeInfo.get("UDF5"))
                    && UtilHelper.isEmpty(receiptDetail.get("LOTTABLE06"))){
                ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的收货批次不允许为空");
            }

            String lottable06 = receiptDetail.get("LOTTABLE06");
            String sku = receiptDetail.get("SKU");

            if(!UtilHelper.isEmpty(lottable06)) {

                if (!receiptLotHashMap.containsKey(lottable06)) receiptLotHashMap.put(lottable06, sku);
                else if (!receiptLotHashMap.get(lottable06).equals(sku))
                    ExceptionHelper.throwRfFulfillLogicException("相同的收货批次不允许接收不同的物料");

                if (!elottable02LotHashMap.containsKey(lottable06)) {
                    elottable02LotHashMap.put(lottable06, receiptDetail.get("ELOTTABLE02"));
                } else if (!UtilHelper.equals(elottable02LotHashMap.get(lottable06), receiptDetail.get("ELOTTABLE02")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的保税状态");

                //除退货类型的配置要求使用原批次的情况外，收货时可不填质量状态，系统会自动根据配置计算得出。
                if (!receiptQAStatusHashMap.containsKey(lottable06)) {
                    receiptQAStatusHashMap.put(lottable06, receiptDetail.get("ELOTTABLE03"));
                } else if (!UtilHelper.equals(receiptQAStatusHashMap.get(lottable06), receiptDetail.get("ELOTTABLE03")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的质量状态");

                if (!receiptInvTypeHashMap.containsKey(lottable06))
                    receiptInvTypeHashMap.put(lottable06, receiptDetail.get("LOTTABLE02"));
                else if (!UtilHelper.equals(receiptInvTypeHashMap.get(lottable06), receiptDetail.get("LOTTABLE02")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的存货类型");


//                if(UtilHelper.isEmpty(receiptDetail.get("ELOTTABLE07"))){
//                    ExceptionHelper.throwRfFulfillLogicException("收货行"+receiptDetail.get("RECEIPTLINENUMBER")+"的型号不允许为空");
//                }else{
                if (!receiptSpecHashMap.containsKey(lottable06))
                    receiptSpecHashMap.put(lottable06, receiptDetail.get("ELOTTABLE07"));
                else if (!UtilHelper.equals(receiptSpecHashMap.get(lottable06), receiptDetail.get("ELOTTABLE07")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的生产批次");
                //}


                if (!supplierCodeHashMap.containsKey(lottable06)) {
                    supplierCodeHashMap.put(lottable06, receiptDetail.get("ELOTTABLE08"));
                } else if (!UtilHelper.equals(supplierCodeHashMap.get(lottable06), receiptDetail.get("ELOTTABLE08")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的供应商");


                if (!supplierLotHashMap.containsKey(lottable06)) {
                    supplierLotHashMap.put(lottable06, receiptDetail.get("ELOTTABLE09"));
                } else if (!UtilHelper.equals(supplierLotHashMap.get(lottable06), receiptDetail.get("ELOTTABLE09")))
                    ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的供应商批次");

                //目前UI界面没有允许录入采购编码，只校验合法性，不硬性设置必填校验
//            if (!purchaseCodeHashMap.containsKey(lottable06))
//                purchaseCodeHashMap.put(lottable06, receiptDetail.get("LOTTABLE10"));
//            else if (!UtilHelper.equals(purchaseCodeHashMap.get(lottable06),receiptDetail.get("LOTTABLE10")))
//                ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的采购编码");
//

                if (UtilHelper.isEmpty(receiptDetail.get("LOTTABLE04"))) {
                    ExceptionHelper.throwRfFulfillLogicException("收货行" + receiptDetail.get("RECEIPTLINENUMBER") + "的收货时间不允许为空");
                } else {

//                    if(receivedDate==null) receivedDate = receiptDetail.get("LOTTABLE04");
//                    if(!receivedDate.equals(receiptDetail.get("LOTTABLE04")))
//                        ExceptionHelper.throwRfFulfillLogicException("同一收货单中的收货时间必须相同");

                    if (!lottable04HashMap.containsKey(lottable06)) {
                        lottable04HashMap.put(lottable06, receiptDetail.get("LOTTABLE04"));
                    } else if (!UtilHelper.equals(lottable04HashMap.get(lottable06), receiptDetail.get("LOTTABLE04")))
                        ExceptionHelper.throwRfFulfillLogicException("收货批次" + lottable06 + "存在不同的收货时间");


                }
            }

        }
    }

    public static void checkSerialNumberExistInInv( Map<String, String> receiptDetail) throws Exception {

        if(SKU.isSerialControl( receiptDetail.get("SKU")) ){

            //校验物料+唯一码在库存中不存在。

            List<Map<String,String>> snList = LotxId.findDetailsByReceiptLineAndLpn( receiptDetail.get("RECEIPTKEY"),receiptDetail.get("RECEIPTLINENUMBER"),receiptDetail.get("TOID"),true);

            for(Map<String,String> snInfo : snList) {

                String sn = snInfo.get("SERIALNUMBERLONG");
                Map<String, String> snHashMap = SerialInventory.findBySkuAndSN( receiptDetail.get("SKU"), sn, false);
                if (snHashMap != null) ExceptionHelper.throwRfFulfillLogicException("箱号"+receiptDetail.get("TOID")+"中的唯一码" + sn + "在库存中已存在，不允许重复收货");

            }
        }

    }

    //UDF1 物料类型
    //UDF2 字段名
    //UDF3 中文名
    //UDF4 是否必填

    private static void checkReceiptMandatoryFields(Map<String, String> receiptDetail, List<Map<String, String>> skuLotConfList) {

        if(skuLotConfList!=null) {
            for (Map<String, String> skuLotConf : skuLotConfList){

                if("Y".equalsIgnoreCase(skuLotConf.get("UDF4"))
                        && receiptDetail.containsKey(skuLotConf.get("UDF2"))
                        && UtilHelper.isEmpty(receiptDetail.get(skuLotConf.get("UDF2")))){
                    ExceptionHelper.throwRfFulfillLogicException(skuLotConf.get("UDF3")+"不允许为空");

                }

            }
        }


    }
    public static void checkMustInputWidget(Map<String, String> receiptInfo ){
        String mustInputWidget=CodeLookup.getCodeLookupValue("RECEIPTYPE",receiptInfo.get("TYPE"),"UDF10","必输字段");
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
                    if (arr.length==3&&"6".equals(arr[2])){
                        if (UtilHelper.isEmpty(receiptInfo.get(UtilHelper.trim(arr[0])))) {
                            errMsg.add(arr[1]);
                        }
                    }

                }
                if (!errMsg.isEmpty()) {
                    ExceptionHelper.throwRfFulfillLogicException("收货单字段" + errMsg.toString() + "为空请填写后操作");
                }
            }
    }   }

    public static void checkASNReceiptCheckStatus(Map<String, String> receiptInfo){
        String isCheck=CodeLookup.getCodeLookupValue("RECEIPTYPE",receiptInfo.get("TYPE"),"EXT_UDF_STR6","必输字段");
        if ("Y".equalsIgnoreCase(isCheck) &&!"3".equalsIgnoreCase(receiptInfo.get("RECEIPTCHECKSTATUS")))
            ExceptionHelper.throwRfFulfillLogicException("收货单未检查请检查后操作");

    }
}
