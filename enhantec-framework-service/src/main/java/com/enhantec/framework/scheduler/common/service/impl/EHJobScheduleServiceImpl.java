package com.enhantec.framework.scheduler.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
import com.enhantec.framework.scheduler.common.mapper.EHJobScheduleMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @author johnw
* @description 针对表【eh_job_schedule】的数据库操作Service实现
* @createDate 2022-12-24 12:56:49
*/
@Service
@DS(DSConstants.DS_ADMIN)
public class EHJobScheduleServiceImpl extends EHBaseServiceImpl<EHJobScheduleMapper, EHJobScheduleModel>
    implements EHJobScheduleService {

    public Page<Map<String, Object>> getPageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().queryPageData(page, qw);

    }

}




