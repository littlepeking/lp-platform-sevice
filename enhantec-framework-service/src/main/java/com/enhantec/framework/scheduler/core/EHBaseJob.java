package com.enhantec.framework.scheduler.core;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.config.MultiDataSourceConfig;

public interface EHBaseJob {

    @DS(DSConstants.DS_DEFAULT)
    void run( String orgId, String[] args);

    /**
     * 可根据实际需要进行覆盖
     * @param orgId
     * @param args
     * @return
     */
    @DS(DSConstants.DS_MASTER)
    default String getBatchDataSource(String orgId, String[] args){
        return MultiDataSourceConfig.DATA_SOURCE_ORG_PREFIX + orgId;

    }
}
