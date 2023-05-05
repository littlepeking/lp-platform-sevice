package com.enhantec.framework.scheduler.common.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author johnw
* @description 针对表【eh_job_schedule】的数据库操作Mapper
* @createDate 2022-12-24 12:56:49
* @Entity com.enhantec.framework.scheduler.common.EHJobSchedule
*/
public interface EHJobScheduleMapper extends EHBaseMapper<EHJobScheduleModel> {

    @MapKey("id")
    Page<Map<String, Object>> queryPageData(@Param("page") Page<Map<String, Object>> page, @Param("ew") QueryWrapper qw);

}




