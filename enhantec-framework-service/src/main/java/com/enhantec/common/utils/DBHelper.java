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


import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.enhantec.Application;

import javax.sql.DataSource;
import java.util.Map;

public class DBHelper {

    public static DataSource getOrgDBName(String orgId) {
        Map<String, DataSource> map =Application.getAppContext().getBeansOfType(DataSource.class);
       return ((DynamicRoutingDataSource) map.values().stream().findFirst().get()).getDataSource(orgId);
    }

}
