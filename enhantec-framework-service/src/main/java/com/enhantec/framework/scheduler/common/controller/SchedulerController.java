/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.common.controller;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import com.enhantec.framework.scheduler.core.JobManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final JobManager jobManager;

    @PreAuthorize("hasAuthority('SCHEDULE_SAVE')")
    @GetMapping("/save")
    public void save(@RequestBody @NotNull EHJobScheduleModel jobSchedule){
        var jobScheduleModel = jobSchedule;
        if(!"0".equals(EHContextHelper.getCurrentOrgId())) {
            jobScheduleModel = jobSchedule.toBuilder().orgId(EHContextHelper.getCurrentOrgId()).build();
        }
        jobManager.saveSchedule(jobScheduleModel);
    }

    @PreAuthorize("hasAuthority('SCHEDULE_REMOVE')")
    @GetMapping("/remove/{scheduleId}")
    public void removeByScheduleId(@NotNull @PathVariable String scheduleId){
        jobManager.removeSchedule(scheduleId);
    }

    @PreAuthorize("hasAuthority('SCHEDULE_RUN')")
    @GetMapping("/runBySchedulerId/{scheduleId}")
    public void runBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.runSchedule(scheduleId);
    }

    @PreAuthorize("hasAuthority('SCHEDULE_STOP')")
    @GetMapping("/stopBySchedulerId/{scheduleId}")
    public void stopBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.stopSchedule(scheduleId);
    }

}
