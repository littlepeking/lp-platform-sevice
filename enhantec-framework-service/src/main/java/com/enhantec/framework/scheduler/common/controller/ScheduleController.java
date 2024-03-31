/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/

package com.enhantec.framework.scheduler.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
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

    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void removeByScheduleId(@NotNull @RequestBody String scheduleId){
        jobManager.removeSchedule(scheduleId);
    }

    @PostMapping("/run/{scheduleId}")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void runBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.runSchedule(scheduleId);
    }

    @PostMapping("/stop/{scheduleId}")
    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    public void stopBySchedulerId(@NotNull @PathVariable String scheduleId){
        jobManager.stopSchedule(scheduleId);
    }


    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams, EHFieldNameConversionType.CAMELCASE2UNDERSCORE);

        Page<Map<String, Object>> res = ehJobScheduleService.getPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }
}
