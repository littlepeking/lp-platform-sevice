package com.enhantec.wms.backend.common.base.code;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

public class CDOrderType {

    public static boolean isRepackOrderType( String orderType){

        Map<String, String> codeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);

        return  "Y".equalsIgnoreCase(codeHashMap.get("UDF8"));

    }

    public static boolean ignoreProjectCodeWhenAlloc( String orderType){

        Map<String,String> codeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);

        return "Y".equalsIgnoreCase(codeHashMap.get("UDF9"));

    }

    /**
     * 是否自动生成箱号


     * @param orderType
     * @return
     */
    public static boolean isBindAndAutoGenerateLpn( String sku, String orderType){

        Map<String, String> codelkup = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);

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

    public static boolean isBindAndNOTAutoGenerateLpn( String sku, String orderType){

        Map<String, String> codelkup = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("1".equals(codelkup.get("UDF3"))) {
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
     * @return
     */
    public static boolean isBindingNewLpn(String sku, String orderType){

        Map<String, String> codelkup = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("1".equals(codelkup.get("UDF3")) || "2".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn( sku);
        }
    }


    public static boolean isKeepOrderQtyAfterShortPick(String orderType){

        Map<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);
        if("1".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }



    public static boolean isReduceOrderQtyAfterShortPick(String orderType){

        Map<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);
        if("2".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }


    public static boolean isSplitTaskAfterShortPick(String orderType){

        Map<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);
        if("3".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }

    public static boolean isAllowOverPick(String orderType){

        Map<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderType);
        if("Y".equalsIgnoreCase(orderTypeHashMap.get("UDF4"))){
            return true;
        }else {
            return false;
        }
    }



}
