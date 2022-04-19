package com.enhantec.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


public class EHPaginationHelper {


    public static Page<Map<String, Object>> buildPageInfo(PageParams pageParams) {
        Page<Map<String,Object>> mapPage = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        return mapPage;
    }

    public static QueryWrapper buildQueryWrapperByPageParams(PageParams pageParams){

        val queryWrapper  =  Wrappers.query();

        if( pageParams.getFilters()!=null) {
            pageParams.getFilters().entrySet().stream().forEach(e ->
            {
                if(e.getValue()!=null && !StringUtils.isBlank(e.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key
                    queryWrapper.eq(e.getKey().replaceAll("\\s+", ""), e.getValue());
                }
            });
        }
        if( pageParams.getExtraParams()!=null) {
            pageParams.getExtraParams().entrySet().stream().forEach(e ->
            {
                if(e.getValue()!=null && !StringUtils.isBlank(e.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key
                    queryWrapper.eq(e.getKey().replaceAll("\\s+", ""), e.getValue());
                }
            });
        }

        if( pageParams.getOrderBy()!=null) {
            pageParams.getOrderBy().entrySet().stream().forEach(e ->
            {
                //removes all whitespaces and non-visible characters (e.g., tab, \n) in key
                queryWrapper.orderBy(true,
                        "asc".equalsIgnoreCase(e.getValue()),
                        e.getKey().replaceAll("\\s+", ""));
            });
        }

        return queryWrapper;
    }

    public static void formatPageData(Page<Map<String,Object>> page){

      var formattedRecords = page.getRecords().stream().map(r->camelCaseMap(r)).collect(Collectors.toList());

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
            String newKey = toFormatCol(key);
            newMap.put(newKey, entry.getValue());
        }
        return newMap;
    }

    private static String toFormatCol(String colName) {
        StringBuilder sb = new StringBuilder();
        String[] str = colName.toLowerCase().split("_");
        int i = 0;
        for (String s: str) {
            if (s.length() == 1) {
                s = s.toUpperCase();
            }
            i++;
            if (i == 1) {
                sb.append(s);
                continue;
            }
            if (s.length()> 0) {
                sb.append(s.substring(0, 1).toUpperCase());
                sb.append(s.substring(1));
            }
        }
        return sb.toString();
    }

}
