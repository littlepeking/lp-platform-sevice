package com.enhantec.wms.backend.inbound.asn.utils;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

public class ReceiptUtilHelper {


    /**
     * 根据订单退货类型的配置更新批次号


     * @param RECEIPTYPE
     * @param receiptDetails 待计算收货批次的收货明细
     * @param returnLotHashMap //当前RECEIPT的新老收货批次对应关系
     * @throws Exception
     */
    public static void updateReturnReceiptlot( String RECEIPTYPE, List<HashMap<String, String>> receiptDetails, HashMap<String, String> returnLotHashMap) throws Exception {

        HashMap<String,String>  receiptTypeInfo = CodeLookup.getCodeLookupByKey("RECEIPTYPE", RECEIPTYPE);

        //0: 退货的批次号直接使用原收货批次号
        //1: 当容器开封时，退货的批次号为原批次号+R(A-Z)
        //2: 退货的批次号直接从系统获取下一新的收货批次号，且Elottable07生产批号+R(A-Z)


        if("0".equals(receiptTypeInfo.get("UDF6"))) {
            //使用原收货批次号,无需修改批次
        }else if("1".equals(receiptTypeInfo.get("UDF6"))) {
            //1: 当容器开封时，退货的批次号为原批次号+R(A-Z)
            if (receiptDetails != null && receiptDetails.size() > 0) {

                for (HashMap<String, String> rd : receiptDetails) {

                    if (("1").equals(rd.get("SUSR7"))) {//开封标记
                        String newRetLot = returnLotHashMap.get(rd.get("LOTTABLE06"));

                        if (newRetLot == null) {
                            //  newRetLot = IdGenerationHelper.generateID(
                            //          context, userid,
                            //          rd.get("LOTTABLE06") + "R",
                            //          2);

                            newRetLot = IdGenerationHelper.createSubReceiptLot( rd.get("LOTTABLE06"), "R");
                            returnLotHashMap.put(rd.get("LOTTABLE06"), newRetLot);

                        }

                        DBHelper.executeUpdate( "UPDATE RECEIPTDETAIL SET LOTTABLE06 = ? WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER  = ? ",
                                new Object[]{newRetLot, rd.get("RECEIPTKEY"), rd.get("RECEIPTLINENUMBER")});
                    }
                }
            }


        }else if("2".equals(receiptTypeInfo.get("UDF6"))) {
            //2: 退货的批次号直接从系统获取下一新的收货批次号，且Elottable07生产批号+R(A-Z)

            for (HashMap<String, String> rd : receiptDetails) {

                //获取下一新的收货批次号
                String newRetLot = returnLotHashMap.get(rd.get("LOTTABLE06"));

                if (newRetLot == null) {
                    newRetLot = IdGenerationHelper.createReceiptLot( rd.get("SKU"));
                    returnLotHashMap.put(rd.get("LOTTABLE06"), newRetLot);

                }

                //Elottable07生产批号+R(A-Z)
                HashMap<String, String> elottable07HashMap = new HashMap<>();
                String newELottable07 = elottable07HashMap.get(rd.get("ELOTTABLE07"));

                if (newELottable07 == null) {
                    //  newRetLot = IdGenerationHelper.generateID(
                    //          context, userid,
                    //          rd.get("LOTTABLE06") + "R",
                    //          2);

                    newELottable07 = IdGenerationHelper.createSubReceiptLot( rd.get("ELOTTABLE07"), "R");
                    elottable07HashMap.put(rd.get("ELOTTABLE07"), newELottable07);

                }

                DBHelper.executeUpdate(
                        "UPDATE RECEIPTDETAIL SET LOTTABLE06 = ? , ELOTTABLE07 = ? WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER  = ? ",
                        new Object[]{
                                newRetLot,
                                newELottable07,
                                rd.get("RECEIPTKEY"),
                                rd.get("RECEIPTLINENUMBER")});

            }
        }
    }
    public static BigDecimal stdQty2PoWgt( String avgSNwgt, BigDecimal LpnQty, String sku) throws Exception {
        if (SKU.isSerialControl(sku)&& CDSysSet.enableSNwgt()){
            if (UtilHelper.isEmpty(avgSNwgt))throw new Exception("物料未配置唯一码平均重量");
            return LpnQty.multiply(new BigDecimal(avgSNwgt));
        }
        return LpnQty;
    }
    public static BigDecimal poWgt2StdQty( String poWgt, String sku) throws Exception {
        if (SKU.isSerialControl(sku)&&CDSysSet.enableSNwgt()) {
            String conversion = SKU.findById( sku, true).get("SNAVGWGT");
            if (UtilHelper.isEmpty(conversion)) throw new Exception("物料未配置唯一码平均重量");
            BigDecimal oldQTY = new BigDecimal(poWgt);
            BigDecimal conversionBigDecimal = new BigDecimal(conversion);
            BigDecimal newQTY = oldQTY.divide(conversionBigDecimal, 0, BigDecimal.ROUND_HALF_UP);
            return newQTY;
        }
        return new BigDecimal(poWgt);
    }
}
