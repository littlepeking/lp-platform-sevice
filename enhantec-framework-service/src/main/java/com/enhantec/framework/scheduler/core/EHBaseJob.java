package com.enhantec.framework.scheduler.core;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.config.MultiDataSourceConfig;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import lombok.Data;

@Data
public abstract class EHBaseJob {

    private EHJobScheduleModel schedule;

    private EHJobDefinitionModel jobDefinition;

    @DS(DSConstants.DS_DEFAULT)
    public abstract void run( String orgId, String[] args);

    /**
     * 可根据实际需要进行覆盖
     * @param orgId
     * @param args
     * @return
     */
    @DS(DSConstants.DS_ADMIN)
    public String getBatchDataSource(String orgId, String[] args){
        return MultiDataSourceConfig.DATA_SOURCE_ORG_PREFIX + orgId;

    }
}
