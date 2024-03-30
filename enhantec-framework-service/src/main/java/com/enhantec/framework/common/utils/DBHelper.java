/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.common.utils;


import com.enhantec.framework.config.annotations.converter.IFieldNameConverter;
import com.enhantec.framework.config.mybatisplus.MybatisPlusConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;

public class DBHelper {

//    public static DataSource getOrgDBName(String orgId) {
//        Map<String, DataSource> map =Application.getAppContext().getBeansOfType(DataSource.class);
//       return ((DynamicRoutingDataSource) map.values().stream().findFirst().get()).getDataSource(orgId);
//    }


    /**
     * Convert the key in the Map from database column Name to java object field Name
     *
     * @param map
     * @return
     */
    public static Map<String, Object> convertColumnName2FieldNameForMap(Map<String, Object> map, IFieldNameConverter fieldNameConverter4Request) {


        IFieldNameConverter fieldNameConverter = fieldNameConverter4Request != null ? fieldNameConverter4Request : MybatisPlusConfig.getDefaultFieldNameConverter();

        Map<String, Object> newMap = new HashMap<>();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            String newKey = fieldNameConverter.convertColumnName2FieldName(key);
            newMap.put(newKey, entry.getValue());
        }
        return newMap;
    }

    public static String camelCase2Snake(String string) {
        string = string.replaceAll("\\s+", "");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);

            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }


    public static String snake2CamelCase(String inputString) {

        if(inputString== null || !inputString.contains("_")) return inputString;

        inputString = inputString.replaceAll("\\s+", "");

        StringBuilder sb = new StringBuilder();

        boolean capitalizeNext = false;

        for (char c : inputString.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }

        return sb.toString();

    }



    public static void convertSqlParamListObj2String(List<Object> list){

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) instanceof LocalDateTime) list.set(i, convertSqlParamObj2String(list.get(i)));
        }

    }

    /**
     * resolve the issue of localDateTime conversion for default serializing format "yyy-MM-ddTHH:mm:ss"
     * @param parameter
     * @return
     */
    public static String convertSqlParamObj2String(Object parameter){

        return parameter == null ? null : parameter instanceof LocalDateTime ?
                convertLocalDateTime2SqlString((LocalDateTime)parameter): parameter.toString();

    }

    public static String convertLocalDateTime2SqlString(LocalDateTime localDateTime){

        DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return localDateTime.format(dataTimeFormatter);


    }


}
