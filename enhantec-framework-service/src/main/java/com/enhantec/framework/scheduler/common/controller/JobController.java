/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.common.controller;

import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.core.JobManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor

public class JobController {

    JobManager jobManager;

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SCHEDULER_JOB')")
    public void saveJob(@RequestBody @NotNull EHJobDefinitionModel jobDefinition){
        jobManager.saveJob(jobDefinition);
    }

    @GetMapping("/remove/{jobId}")
    @PreAuthorize("hasAuthority('SCHEDULER_JOB')")
    public void removeJob(@PathVariable @NotNull String jobId){
        jobManager.removeJob(jobId);
    }

}