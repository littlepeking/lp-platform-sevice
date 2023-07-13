package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.DBResourceException;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.sql.Connection;
import java.util.HashMap;

public class STORER {

    public static HashMap<String, String> getSupplier(Context context, Connection conn, String supplier, String sku) throws Exception {
        String conf= CodeLookup.getCodeLookupValue(context,conn,"SYSSET","SUPPLIER","UDF1","");
        if ("SKUSUPPLIER".equals(conf)){
            return getSupplierFromSkuSupplierTable(context,conn,supplier,sku);
        }else if ("SUPPLIER".equals(conf)){
        return getSupplierFromSupplierTable(context,conn,supplier,"5");
        }else if ("SKU".equals(conf)){
            return getSupplierFromSkuTable(context,conn,supplier,sku);
        }else {
            HashMap<String,String> hashMap =new HashMap<>();
            hashMap.put("COMPANY","");
            return hashMap;
        }
    }
    //根据供应商代码查供应商名称
    public static HashMap<String, String> getSupplierFromSupplierTable(Context context,Connection conn,String storerkey,String type) throws DBResourceException{
        HashMap<String, String> storer = DBHelper.getRecord(context,conn,"SELECT * FROM STORER s " +
                " WHERE  s.STORERKEY =? AND s.[TYPE] =?",new Object[]{storerkey,type},"供应商信息表");
        if (storer == null ) ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在供应商主数据不存在");
        return storer;
    }
    //根据供应商代码查供应商名称
    public static HashMap<String, String> getSupplierFromSkuSupplierTable(Context context,Connection conn,String storerkey,String sku) throws DBResourceException {
        HashMap<String, String> storer = DBHelper.getRecord(context,conn,"SELECT s.SUPPLIERCODE as SUPPLIER,v.COMPANY FROM SkuSupplier s, V_SUPPLIER v" +
                " WHERE s.SUPPLIERCODE=v.storerkey and s.SUPPLIERCODE =? AND s.SKU =?",new Object[]{storerkey,sku},"供应商信息表");
        if (storer == null )ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在合格供应商主数据不存在");
        return storer;
    }
    //根据供应商代码查供应商名称
    public static HashMap<String, String> getSupplierFromSkuTable(Context context,Connection conn,String storerkey,String sku) throws DBResourceException{
        HashMap<String, String> storer = DBHelper.getRecord(context,conn,"SELECT s.BUSR9 as SUPPLIER,s.BUSR10 as COMPANY FROM SKU s " +
                " WHERE  s.BUSR9 =? AND s.SKU =?",new Object[]{storerkey,sku},"供应商信息表");
        if (storer == null )ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在物料主数据不存在");
        return storer;
    }


}
