package com.enhantec.wms.backend.common.base.code;


import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;

public class CDReceiptType {
    /**
     * 是否是有库存的退货


     * @param type
     * @return
     */
    public static boolean isReturnTypeWithInventory( String type){
        return "R3".equals(CodeLookup.getCodeLookupValue("RECEIPTYPE",type,"UDF5","收货类型"));
    }

    /**
     * 是否是退货类型


     * @param type
     * @return
     */
    public static boolean isReturnType( String type){
        //CODELKUP表的UDF5中前缀为R的类型为退货类型
        String receiptType =CodeLookup.getCodeLookupValue( "RECEIPTYPE", type, "UDF5", "收货类型");
        boolean isReturnType = !UtilHelper.isEmpty(receiptType) && receiptType.startsWith("R");

        return isReturnType;
    }

    /**
     * 是否自动收货
     * @return
     */
    public static boolean isAutoReceiving( String receiptType){

        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);
        if("Y".equalsIgnoreCase(receiptTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 是否自动收货
     * @return
     */
    public static String getReceivingFuncType( String receiptType){

        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);
        return receiptTypeHashMap.get("UDF5");
    }




    public static boolean isBindAndAutoGenerateLpn( String sku, String receiptType) {

        HashMap<String, String> codelkup = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("2".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn( sku);
        }
    }


    /**
     * 是否绑定箱号：目前仅支持扫描唯一码绑定至箱号。扫描容器条码和箱号不允许绑定
     * RECEIPTYPE UDF3
     * @return
     */
    public static boolean isBindingNewLpn(String sku,String receiptType) {

        HashMap<String, String> codelkup = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);

        if(SKU.isSerialControl(sku) && !UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("1".equals(codelkup.get("UDF3")) || "2".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn( sku);
        }
    }


    //是否允许超拣
    public static boolean isAllowOverPick(String receiptType) throws Exception{

        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);
        if("Y".equalsIgnoreCase(receiptTypeHashMap.get("UDF4"))){
            return true;
        }else {
            return false;
        }
    }


    public static String getReturnLotGenerateType(String receiptType){
    /*
            UDF6= 本设置仅用于退货批次
            0: 退货的批次号直接使用原收货批次号
            1: 当容器开封时，退货的批次号为原批次号+R(A-Z)
            2: 退货的批次号直接从系统获取下一新的收货批次号，且Elottable07生产批号+R(A-Z)
     */
        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType);
        return receiptTypeHashMap.get("UDF6");
    }



    /**
     * 是否允许过量收货--即收货行预期量超过指令行的预期量
     * N不允许，default；允许
     * @return
     */
    public static boolean enableExcessReceiving(String receiptType){
        return !"N".equalsIgnoreCase(CodeLookup.getCodeLookupValue("RECEIPTYPE",receiptType,"UDF8","是否允许超收"));
    }


}
