package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;

import java.sql.Connection;
import java.util.Map;

/**
 * 在v_DAAS_ProjectCode表中
 * 针对生基项目和CSS项目，只有Project_id能保障唯一
 */
public class DaasProjectCode {
    /**
     * 根据项目ID获取其它信息
     */
    public static Map<String,String> getByProjectId(Context context, String projectId){
        return DBHelper.getRecord(context,
                "SELECT * FROM v_DAAS_PROJECTCODE WHERE project_id = ?",
                new Object[]{projectId},
                "未找到项目ID"+projectId,true);
    }
}