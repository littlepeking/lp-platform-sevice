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
