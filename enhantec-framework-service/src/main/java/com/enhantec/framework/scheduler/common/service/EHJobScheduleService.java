package com.enhantec.framework.scheduler.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;

import java.util.Map;

/**
* @author johnw
* @description 针对表【eh_job_schedule】的数据库操作Service
* @createDate 2022-12-24 12:56:49
*/
public interface EHJobScheduleService extends EHBaseService<EHJobScheduleModel> {


    Page<Map<String, Object>> getPageData(Page<Map<String, Object>> page, QueryWrapper qw);


}
