/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class EHPaginationHelper {


    public static Page<Map<String, Object>> buildPageInfo(PageParams pageParams) {
        Page<Map<String, Object>> mapPage = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        //John - DO NOT Optimize Join since it will make some join conditions lose when generating page count sql.
        mapPage.setOptimizeJoinOfCountSql(false);
        return mapPage;
    }

    public static QueryWrapper buildQueryWrapperByPageParams(PageParams pageParams) {

        val queryWrapper = Wrappers.query();

        if (pageParams.getFilters() != null) {
            pageParams.getFilters().stream().forEach(f ->
            {
                if (f.getValue() != null && !StringUtils.isBlank(f.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key

                    HashSet<String> singleColumnValues = new HashSet<>();
                    String columnName = formatKey(f.getColumnName());

                    if ("string".equals(f.getType())) {
                        //deal with scenario => multiSelect filter allow empty value
                        boolean searchEmptyValue = ((String) f.getValue()).contains("[__EMPTY__]");

                        queryWrapper.and(wq -> {

                                    singleColumnValues.addAll(Arrays.stream(f.getValue().toString().split(",")).
                                            map(e -> e.replace("[__EMPTY__]", ""))
                                            .collect(Collectors.toList()));

                                    if (singleColumnValues.size() == 1) {
                                        //wq.likeRight(columnName, singleColumnValues.iterator().next());
                                        wq.apply(columnName + " like {0}", singleColumnValues.iterator().next());
                                    } else {
                                        wq.in(columnName, singleColumnValues);
                                    }

                                    if (searchEmptyValue)
                                        wq.or().isNull(columnName);
                                }
                        );
                    } else if ("date".equals(f.getType())) {
                        LocalDateTime dateTime = EHDateTimeHelper.timeStamp2LocalDateTime(f.getValue());
                        queryWrapper.ge(columnName, dateTime);
                        Duration duration = Duration.ofHours(23);
                        duration = duration.plusMinutes(59).plusSeconds(59);
                        //end of the day converted from the given timezone to the GMT timezone
                        queryWrapper.lt(columnName, dateTime.plus(duration));
                    } else {
                        queryWrapper.eq(columnName, f.getValue());
                    }
//
//                    val subQw = Wrappers.query();


                }
            });
        }
        if (pageParams.getParams() != null) {
            pageParams.getParams().entrySet().stream().forEach(e ->
            {
                if (e.getValue() != null && !StringUtils.isBlank(e.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key
                    queryWrapper.eq(formatKey(e.getKey()), e.getValue());
                }
            });
        }

        if (pageParams.getOrderBy() != null) {
            pageParams.getOrderBy().entrySet().stream().forEach(e ->
            {
                queryWrapper.orderBy(true,
                        "asc".equalsIgnoreCase(e.getValue()),
                        formatKey(e.getKey()));
            });
        }

        return queryWrapper;
    }

    private static String formatKey(String key) {

        String trimmedKey = key.replaceAll("\\s+", "");

        return camelToSnake(trimmedKey);
    }

    public static void formatPageData(Page<Map<String, Object>> page) {

        var formattedRecords = page.getRecords().stream().map(r -> camelCaseMap(r)).collect(Collectors.toList());

        page.setRecords(formattedRecords);

    }


    /**
     * Convert the key in the Map from underscore to hump
     *
     * @param map
     * @return
     */
    public static Map<String, Object> camelCaseMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            String newKey = SnakeToCamel(key);
            newMap.put(newKey, entry.getValue());
        }
        return newMap;
    }

    private static String SnakeToCamel(String colName) {
        StringBuilder sb = new StringBuilder();
        String[] str = colName.toLowerCase().split("_");
        int i = 0;
        for (String s : str) {
            if (s.length() == 1) {
                s = s.toUpperCase();
            }
            i++;
            if (i == 1) {
                sb.append(s);
                continue;
            }
            if (s.length() > 0) {
                sb.append(s.substring(0, 1).toUpperCase());
                sb.append(s.substring(1));
            }
        }
        return sb.toString();
    }

    public static String camelToSnake(String str) {

        // Empty String
        String result = "";

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result
                        = result
                        + Character.toLowerCase(ch);
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result = result + ch;
            }
        }

        // return the result
        return result;
    }

}
