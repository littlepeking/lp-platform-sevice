package com.enhantec.wms.backend.utils.common;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.Context;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class POValidationHelper {
        public static void supplierCheck(Context context, String supplier, String sku, String errmsg) throws Exception {
            String conf= CodeLookup.getCodeLookupValue(context,"SUPPLIER","SUPPLIER","UDF1","");
            if ("SKUSUPPLIER".equals(conf)){
            supplierCheckFromSkuSupplier(context,supplier,sku,errmsg);
            }else if ("SUPPLIER".equals(conf)){
                supplierCheckFromSupplier(context,supplier,errmsg);
            }else if ("SKU".equals(conf)){
                supplierCheckFromSku(context,sku,supplier,errmsg);
            }
        }
        public static void supplierCheckFromSkuSupplier(Context context,String supplier,String sku,String errmsg) throws Exception {
            supplierCheckFromSupplier(context,supplier,errmsg);
            int countSKUSUPPLIER = Integer.parseInt((String)DBHelper.getValue(context,"select COUNT(1) from SKUSUPPLIER where SKU=? and SUPPLIERCODE=?",
                    new Object[]{sku,supplier},errmsg));
            if(countSKUSUPPLIER==0)
                throw new Exception(errmsg);
        }
    public static void supplierCheckFromSku(Context context,String supplier,String sku,String errmsg) throws Exception {
        int countSKUSUPPLIER = Integer.parseInt((String)DBHelper.getValue(context,"select COUNT(1) from SKU where SKU=? and BUSR9=?",
                new Object[]{sku,supplier},errmsg));
        if(countSKUSUPPLIER==0)
            throw new Exception(errmsg);
    }
    public static void supplierCheckFromSupplier(Context context,String supplier,String errmsg) throws Exception {
        int countvendor = Integer.parseInt((String)DBHelper.getValue(context,"select COUNT(1) from STORER where TYPE ='5' AND STORERKEY=?",
                new Object[]{supplier},errmsg));
        if(countvendor==0)
            throw new Exception(errmsg);
    }
    public static void supplierValidityCheckFromSupplier(Context context,String supplier,String errmsg) throws Exception {
            supplierCheckFromSupplier(context,supplier,"供应商代码"+supplier+"在供应商主数据中不存在");
        String INACTIVEDATEVendor = DBHelper.getValue(context, "select CONVERT(varchar(100),DATEADD(hh,-11,INACTIVEDATE),120) from STORER where TYPE ='5' AND STORERKEY=?",
                new String[]{supplier},"");
        if (!"".equals(INACTIVEDATEVendor)&&!"NULL".equals(INACTIVEDATEVendor)){
            String dbdateStr = DBHelper.getValue(context, "SELECT CONVERT(varchar(100),getdate(),120)",new String[]{},"");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date INACTIVEDATE=df.parse(INACTIVEDATEVendor);
            Date dbdate=df.parse(dbdateStr);
            if (INACTIVEDATE.getTime()<dbdate.getTime()){
                throw new Exception(errmsg);
            }
        }
    }
    public static void SupplierValidityCheckFromSkuSupplier(Context context,String sku,String supplier,String errmsg) throws Exception {
            supplierCheckFromSkuSupplier(context,supplier,sku,"供应商代码"+supplier+"在批准供应商主数据中不存在");
        String INACTIVEDATEStr = DBHelper.getValue(context, "select CONVERT(varchar(100),DATEADD(hh,-11,INACTIVEDATE),120) from SKUSUPPLIER where SKU=? and SUPPLIERCODE=?",
                new String[]{sku, supplier},"");
        if (!"".equals(INACTIVEDATEStr)&&!"NULL".equals(INACTIVEDATEStr)){
            String dbdateStr = DBHelper.getValue(context, "SELECT CONVERT(varchar(100),getdate(),120)",new String[]{},"");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date INACTIVEDATE=df.parse(INACTIVEDATEStr);
            Date dbdate=df.parse(dbdateStr);
            if (INACTIVEDATE.getTime()<dbdate.getTime()){
                throw new Exception(errmsg);
            }
        }
    }
}
