package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.UserInfo;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.*;

public class CodeLookup {

    public static String getSysConfig(Context context, Connection conn, String configName) {

        return getCodeLookupValue(context, conn, "SYSSET", configName,"UDF1","仓库系统配置");
    }

    public static String getSysConfig(Context context,Connection conn, String code,String defaultValue)  {

        if(code ==null || code.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("查询系统代码时代码不可以为空");

        List<HashMap<String,String>> list = getCodeLookupList(context, conn, "SYSSET");

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



    public static String getCodeLookupValue(Context context,Connection conn, String listName, String key, String field,String errMsg) {


        HashMap<String,String> map = getCodeLookupByKey(context, conn,listName, key);
        if(map==null || map.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到"+errMsg+" 代码列表:"+listName+" 代码: "+key);
        if(!map.containsKey(field)) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到字段配置: "+field);
        return map.get(field);

    }

    public static HashMap<String,String> getCodeLookupByKey(Context context,Connection connection,String listName,String code,String dbId){
        String sql = "SELECT * FROM "+dbId+".CODELKUP WHERE LISTNAME = ? AND CODE =?";
        HashMap<String, String> record = DBHelper.getRecord(context, connection, sql, new Object[]{listName, code}, "", false);
        if(null == record || record.size() == 0){
            ExceptionHelper.throwRfFulfillLogicException("未找到listname="+listName+"，code="+code+"的配置");
        }
        return record;
    }


    public static HashMap<String,String> getCodeLookupByKey(Context context,Connection conn, String listName, String code){


        if(code ==null || code.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("查询系统代码时代码不可以为空");
        List<HashMap<String,String>> list = getCodeLookupList(context, conn, listName);
        if(list ==null || list.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到列表: "+listName);

        for(HashMap<String,String> record: list){
            if(code.trim().equals(record.get("CODE"))){
                return record;
            }
        }

        ExceptionHelper.throwRfFulfillLogicException("在"+listName+"中未找到代码: "+ code);

        return null;

    }

    public static HashMap<String,String> getCodeLookupByKey(UserInfo userInfo, String listName, String code){


        if(code ==null || code.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("查询系统代码时代码不可以为空");
        List<HashMap<String,String>> list = getCodeLookupList(userInfo, listName);
        if(list ==null || list.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("在系统代码中未找到列表: "+listName);

        for(HashMap<String,String> record: list){
            if(code.trim().equals(record.get("CODE"))){
                return record;
            }
        }

        ExceptionHelper.throwRfFulfillLogicException("在"+listName+"中未找到代码: "+ code);

        return null;

    }

    public static List<HashMap<String,String>> getCodeLookupList(Context context,Connection conn, String listName)  {

        List<HashMap<String,String>> list = DBHelper.executeQuery(context, conn, "select * from codelkup where listname=? ", new Object[]{listName});
        return list;

    }

    public static List<HashMap<String,String>> getCodeLookupList(UserInfo userInfo, String listName)  {

        List<HashMap<String,String>> list = DBHelper.executeQuery(userInfo, "SElect * from codelkup where listname=? ", Arrays.asList(new Object[]{listName}));
        return list;

    }


}
