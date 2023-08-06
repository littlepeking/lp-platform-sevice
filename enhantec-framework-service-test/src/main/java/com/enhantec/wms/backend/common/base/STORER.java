package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.DBResourceException;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

import java.util.HashMap;
import java.util.Map;

public class STORER {

    public static Map<String, String> getSupplier( String supplier, String sku) throws Exception {
        String conf= CodeLookup.getCodeLookupValue("SYSSET","SUPPLIER","UDF1","");
        if ("SKUSUPPLIER".equals(conf)){
            return getSupplierFromSkuSupplierTable(supplier,sku);
        }else if ("SUPPLIER".equals(conf)){
        return getSupplierFromSupplierTable(supplier,"5");
        }else if ("SKU".equals(conf)){
            return getSupplierFromSkuTable(supplier,sku);
        }else {
            Map<String,String> hashMap =new HashMap<>();
            hashMap.put("COMPANY","");
            return hashMap;
        }
    }
    //根据供应商代码查供应商名称
    public static Map<String, String> getSupplierFromSupplierTable(String storerkey,String type) throws DBResourceException{
        Map<String, String> storer = DBHelper.getRecord("SELECT * FROM STORER s " +
                " WHERE  s.STORERKEY =? AND s.[TYPE] =?",new Object[]{storerkey,type},"供应商信息表");
        if (storer == null ) ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在供应商主数据不存在");
        return storer;
    }
    //根据供应商代码查供应商名称
    public static Map<String, String> getSupplierFromSkuSupplierTable(String storerkey,String sku) throws DBResourceException {
        Map<String, String> storer = DBHelper.getRecord("SELECT s.SUPPLIERCODE as SUPPLIER,v.COMPANY FROM SkuSupplier s, V_SUPPLIER v" +
                " WHERE s.SUPPLIERCODE=v.storerkey and s.SUPPLIERCODE =? AND s.SKU =?",new Object[]{storerkey,sku},"供应商信息表");
        if (storer == null )ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在合格供应商主数据不存在");
        return storer;
    }
    //根据供应商代码查供应商名称
    public static Map<String, String> getSupplierFromSkuTable(String storerkey,String sku) throws DBResourceException{
        Map<String, String> storer = DBHelper.getRecord("SELECT s.BUSR9 as SUPPLIER,s.BUSR10 as COMPANY FROM SKU s " +
                " WHERE  s.BUSR9 =? AND s.SKU =?",new Object[]{storerkey,sku},"供应商信息表");
        if (storer == null )ExceptionHelper.throwRfFulfillLogicException("供应商代码为:"+storerkey+"在物料主数据不存在");
        return storer;
    }


}
