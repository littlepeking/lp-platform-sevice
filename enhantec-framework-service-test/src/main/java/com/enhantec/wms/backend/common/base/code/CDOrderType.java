package com.enhantec.wms.backend.common.base.code;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.UserInfo;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import java.sql.Connection;
import java.util.HashMap;

public class CDOrderType {

    public static boolean isRepackOrderType(Context context, Connection connection, String orderType){

        HashMap<String, String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "ORDERTYPE", orderType);

        return  "Y".equalsIgnoreCase(codeHashMap.get("UDF8"));

    }

    public static boolean isRepackOrderType(UserInfo userInfo, String orderType){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(userInfo, "ORDERTYPE", orderType);

        return "Y".equalsIgnoreCase(codeHashMap.get("UDF8"));

    }

    public static boolean ignoreProjectCodeWhenAlloc(Context context, Connection connection, String orderType){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "ORDERTYPE", orderType);

        return "Y".equalsIgnoreCase(codeHashMap.get("UDF9"));

    }

    /**
     * 是否自动生成箱号
     * @param context
     * @param connection
     * @param orderType
     * @return
     */
    public static boolean isBindAndAutoGenerateLpn(Context context, Connection connection, String sku, String orderType){

        HashMap<String, String> codelkup = CodeLookup.getCodeLookupByKey(context, connection, "ORDERTYPE", orderType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("2".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn(context, connection, sku);
        }
    }

    public static boolean isBindAndNOTAutoGenerateLpn(Context context, Connection connection, String sku, String orderType){

        HashMap<String, String> codelkup = CodeLookup.getCodeLookupByKey(context, connection, "ORDERTYPE", orderType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("1".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn(context, connection, sku);
        }
    }



    /**
     * 是否绑定箱号：目前仅支持扫描唯一码绑定至箱号。扫描容器条码和箱号不允许绑定
     * @return
     */
    public static boolean isBindingNewLpn(Context context,Connection connection, String sku, String orderType){

        HashMap<String, String> codelkup = CodeLookup.getCodeLookupByKey(context, connection, "ORDERTYPE", orderType);

        if(!UtilHelper.isEmpty(codelkup.get("UDF3"))) {

            if ("1".equals(codelkup.get("UDF3")) || "2".equals(codelkup.get("UDF3"))) {
                return true;
            } else {
                return false;
            }
        }else {
            return SKU.isBindingLpn(context, connection, sku);
        }
    }


    public static boolean isKeepOrderQtyAfterShortPick(Context context,Connection connection,String orderType){

        HashMap<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey(context,connection, "ORDERTYPE", orderType);
        if("1".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }



    public static boolean isReduceOrderQtyAfterShortPick(Context context,Connection connection,String orderType){

        HashMap<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey(context,connection, "ORDERTYPE", orderType);
        if("2".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }


    public static boolean isSplitTaskAfterShortPick(Context context,Connection connection,String orderType){

        HashMap<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey(context,connection, "ORDERTYPE", orderType);
        if("3".equals(orderTypeHashMap.get("UDF2"))){
            return true;
        }else {
            return false;
        }
    }

    public static boolean isAllowOverPick(Context context,Connection connection,String orderType){

        HashMap<String,String> orderTypeHashMap = CodeLookup.getCodeLookupByKey(context,connection, "ORDERTYPE", orderType);
        if("Y".equalsIgnoreCase(orderTypeHashMap.get("UDF4"))){
            return true;
        }else {
            return false;
        }
    }



}
