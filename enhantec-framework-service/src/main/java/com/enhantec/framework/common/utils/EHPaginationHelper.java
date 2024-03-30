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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.config.annotations.converter.IFieldNameConverter;
import com.enhantec.framework.config.mybatisplus.MybatisPlusConfig;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;


public class EHPaginationHelper {


    public static  Page buildPageInfo(PageParams pageParams) {
        Page page = new Page(pageParams.getPageNum(), pageParams.getPageSize());
        //John - DO NOT Optimize Join since it will make some join conditions lose when generating page count sql.
        page.setOptimizeJoinOfCountSql(false);
        return page;
    }

    public static QueryWrapper buildQueryWrapperByPageParams(PageParams pageParams){
        return buildQueryWrapperByPageParams(pageParams,null);
    }

    public static QueryWrapper buildQueryWrapperByPageParams(PageParams pageParams, IFieldNameConverter fieldNameConverter4Request) {

        IFieldNameConverter fieldNameConverter = fieldNameConverter4Request != null ? fieldNameConverter4Request : MybatisPlusConfig.getDefaultFieldNameConverter();

        val queryWrapper = Wrappers.query();

        if (pageParams.getFilters() != null) {
            pageParams.getFilters().stream().forEach(f ->
            {
                if (f.getValue() != null && !StringUtils.isBlank(f.getValue().toString())) {
                    //removes all whitespaces and non-visible characters (e.g., tab, \n) in key

                    HashSet<String> singleColumnValues = new HashSet<>();
                    String columnName = fieldNameConverter.convertFieldName2ColumnName(f.getColumnName());

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
                        //The timestamp passed here should be the 12AM of user timezone.
                        LocalDateTime startDateTime = EHDateTimeHelper.timeStamp2LocalDateTime(f.getValue());
                        queryWrapper.ge(columnName, startDateTime);
                        Duration duration = Duration.ofHours(23);
                        duration = duration.plusMinutes(59).plusSeconds(59);
                        //end of the day converted from the above startDateTime + 24 hours
                        //The above conversion make sure the result of query match the timeframe of user timezone.
                        queryWrapper.lt(columnName, startDateTime.plus(duration));
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
                    queryWrapper.eq(fieldNameConverter.convertFieldName2ColumnName(e.getKey()), e.getValue());
                }
            });
        }

        if (pageParams.getOrderBy() != null) {
            pageParams.getOrderBy().entrySet().stream().forEach(e ->
            {
                queryWrapper.orderBy(true,
                        "asc".equalsIgnoreCase(e.getValue()),
                        fieldNameConverter.convertFieldName2ColumnName(e.getKey()));
            });
        }

        return queryWrapper;
    }


    /**
     * Only use when default Page's Map conversion logic of MyMapWrapper cannot satisfy the requirement.
     * @param page
     * @param fieldNameConverter4Request
     */
    public static void convertFieldNameByPage(Page<Map<String, Object>> page, IFieldNameConverter fieldNameConverter4Request) {

        var formattedRecords = page.getRecords().stream().map(r -> DBHelper.convertColumnName2FieldNameForMap(r,fieldNameConverter4Request)).collect(Collectors.toList());

        page.setRecords(formattedRecords);

    }

    public static void convertFieldNameByPage(Page<Map<String, Object>> page) {

        convertFieldNameByPage(page,null);

    }





}
