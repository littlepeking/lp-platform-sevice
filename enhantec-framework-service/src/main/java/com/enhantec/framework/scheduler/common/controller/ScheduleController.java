/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
import com.enhantec.framework.scheduler.core.JobManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final JobManager jobManager;

    private final EHJobScheduleService ehJobScheduleService;


    @PreAuthorize("hasAnyAuthority('SCHEDULER_SCHEDULE')")
    @GetMapping("/findById/{id}")
    public EHJobScheduleModel findById(@NotNull @PathVariable String id) {
        return ehJobScheduleService.getById(id);
    }

    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    @PostMapping("/save")
    public EHJobScheduleModel save(@RequestBody @NotNull EHJobScheduleModel jobSchedule){
        var jobScheduleModel = jobSchedule;
        if(!"0".equals(EHContextHelper.getCurrentOrgId())) {
            jobScheduleModel = jobSchedule.toBuilder().orgId(EHContextHelper.getCurrentOrgId()).build();
        }
       return jobManager.saveSchedule(jobScheduleModel);
    }

    @GetMapping("/remove/{scheduleId}")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void removeByScheduleId(@NotNull @PathVariable String scheduleId){
        jobManager.removeSchedule(scheduleId);
    }

    @GetMapping("/run/{scheduleId}")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void runBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.runSchedule(scheduleId);
    }

    @GetMapping("/stop/{scheduleId}")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void stopBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.stopSchedule(scheduleId);
    }


    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = ehJobScheduleService.getPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }
}
