package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.*;

public class CodeLookup {

    public static String getSysConfig( String configName) {

        return getCodeLookupValue( "SYSSET", configName,"UDF1","仓库系统配置");
    }

    public static String getSysConfig( String code,String defaultValue)  {

        if(code ==null || code.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("查询系统代码时代码不可以为空");

        List<HashMap<String,String>> list = getCodeLookupList( "SYSSET");

        if(list.isEmpty()){

            return defaultValue;

        }else {

            for (HashMap<String, String> record : list) {
                if (code.trim().equals(record.get("CODE"))) {
                    return UtilHelper.isEmpty(record.get("UDF1")) ? defaultValue: record.get("UDF1");
                }
            }

        }

        return defaultValue;

    }



    public static String getCodeLookupValue( String listName, String key, String field,String errMsg) {


        HashMap<String,String> map = getCodeLookupByKey(listName, key);
        if(map==null || map.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到"+errMsg+" 代码列表:"+listName+" 代码: "+key);
        if(!map.containsKey(field)) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到字段配置: "+field);
        return map.get(field);

    }

    public static HashMap<String,String> getCodeLookupByKey(String listName,String code,String dbId){
        String sql = "SELECT * FROM "+dbId+".CODELKUP WHERE LISTNAME = ? AND CODE =?";
        HashMap<String, String> record = DBHelper.getRecord( sql, new Object[]{listName, code}, "", false);
        if(null == record || record.size() == 0){
            ExceptionHelper.throwRfFulfillLogicException("未找到listname="+listName+"，code="+code+"的配置");
        }
        return record;
    }

    public static HashMap<String,String> getCodeLookupByKey(String listName, String code){


        if(code ==null || code.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("查询系统代码时代码不可以为空");
        List<HashMap<String,String>> list = getCodeLookupList(listName);
        if(list ==null || list.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到列表: "+listName);

        for(HashMap<String,String> record: list){
            if(code.trim().equals(record.get("CODE"))){
                return record;
            }
        }

        ExceptionHelper.throwRfFulfillLogicException("在"+listName+"中未找到代码: "+ code);

        return null;

    }

    public static List<HashMap<String,String>> getCodeLookupList( String listName)  {

        List<HashMap<String,String>> list = DBHelper.executeQuery( "select * from codelkup where listname=? ", new Object[]{listName});
        return list;

    }

}
