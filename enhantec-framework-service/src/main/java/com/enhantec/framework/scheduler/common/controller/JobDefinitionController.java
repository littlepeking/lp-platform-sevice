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
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.core.JobManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler/jobDefinition")
@RequiredArgsConstructor
public class JobDefinitionController {


    private final EHJobDefinitionService ehJobDefinitionService;

    private final JobManager jobManager;

    @PreAuthorize("hasAnyAuthority('SCHEDULER_JOB_DEFINITION')")
    @GetMapping("/findById/{id}")
    public EHJobDefinitionModel findById(@NotNull @PathVariable String id) {
        return ehJobDefinitionService.getById(id);
    }


    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SCHEDULER_JOB_DEFINITION')")
    public EHJobDefinitionModel saveJob(@RequestBody @NotNull EHJobDefinitionModel jobDefinition){
      return  jobManager.saveJob(jobDefinition);
    }

    @PreAuthorize("hasAnyAuthority('SCHEDULER_JOB_DEFINITION')")
    @DeleteMapping("")
    public void delete(@RequestBody @NotNull String id) {
        jobManager.removeJob(id);
    }


    @GetMapping("/remove/{jobId}")
    @PreAuthorize("hasAuthority('SCHEDULER_JOB_DEFINITION')")
    public void removeJob(@PathVariable @NotNull String jobId){
        jobManager.removeJob(jobId);
    }

    @PreAuthorize("hasAnyAuthority('SCHEDULER_JOB_DEFINITION')")
    @PostMapping("/queryByPage")
    public Page<EHJobDefinitionModel> queryByPage(@RequestBody PageParams pageParams) {

        Page pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<EHJobDefinitionModel> res = ehJobDefinitionService.page(pageInfo, queryWrapper);

        return res;
    }
}
