/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JobManager implements DisposableBean {


    private final ConcurrentHashMap<String, Job> scheduledJobs = new ConcurrentHashMap();

    private final TaskScheduler taskScheduler;

    public void removeJob(Job job) {
        if (job != null && scheduledJobs.containsKey(job.getJobDefinition().getId())) {
            scheduledJobs.get(job.getJobDefinition().getId()).cancel();
            this.scheduledJobs.remove(job.getJobDefinition().getId());
        }

    }

    public void addJob(Job job) {
        if(job != null){
            removeJob(job);
            job.setScheduledJobInstance(this.taskScheduler.schedule(job,new CronTrigger(job.getJobDefinition().getCronExpression())));
            scheduledJobs.put(job.getJobDefinition().getId(),job);
        }

    }


    @Override
    public void destroy() {
        for (Job job : this.scheduledJobs.values()) {
            job.cancel();
        }

        this.scheduledJobs.clear();
    }
}
