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



package com.enhantec.framework.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;


public class EHPaginationHelper {


    public static <T> Page<T> buildPageInfo(PageParams pageParams) {
        Page<T> page = new Page(pageParams.getPageNum(), pageParams.getPageSize());
        //John - DO NOT Optimize Join since it will make some join conditions lose when generating page count sql.
        page.setOptimizeJoinOfCountSql(false);
        return page;
    }

    public static QueryWrapper buildQueryWrapperByPageParams(PageParams pageParams) {

        val queryWrapper = Wrappers.query();

        if (pageParams.getFilters() != null) {
            pageParams.getFilters().stream().forEach(f ->
            {
                if (f.getValue() != null && !StringUtils.isBlank(f.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key

                    HashSet<String> singleColumnValues = new HashSet<>();
                    String columnName = DBHelper.formatCamelKey2Snake(f.getColumnName());

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
                    queryWrapper.eq(DBHelper.formatCamelKey2Snake(e.getKey()), e.getValue());
                }
            });
        }

        if (pageParams.getOrderBy() != null) {
            pageParams.getOrderBy().entrySet().stream().forEach(e ->
            {
                queryWrapper.orderBy(true,
                        "asc".equalsIgnoreCase(e.getValue()),
                        DBHelper.formatCamelKey2Snake(e.getKey()));
            });
        }

        return queryWrapper;
    }



    public static void formatPageData(Page<Map<String, Object>> page) {

        var formattedRecords = page.getRecords().stream().map(r -> DBHelper.camelCaseMap(r)).collect(Collectors.toList());

        page.setRecords(formattedRecords);

    }



}
