package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;

import java.sql.Connection;
import java.util.List;

public class System {

    public static List<String> getEnableWarehouses(Context context){
        String sql = "SELECT db_logid FROM SCPRD.wmsadmin.pl_db pd WHERE pd.db_logid LIKE 'wmwhse%' AND isActive  = '1'";
        return DBHelper.getValueList(context, sql, new Object[]{}, "");
    }
}
