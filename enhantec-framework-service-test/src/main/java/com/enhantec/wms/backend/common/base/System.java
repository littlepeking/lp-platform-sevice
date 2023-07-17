package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.List;

public class System {

    public static List<String> getEnableWarehouses(){
        String sql = "SELECT db_logid FROM SCPRD.wmsadmin.pl_db pd WHERE pd.db_logid LIKE 'wmwhse%' AND isActive  = '1'";
        return DBHelper.getValueList( sql, new Object[]{}, "");
    }
}
