/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.scheduler.common.model.EHJobDefinition;
import com.enhantec.framework.scheduler.common.model.EHJobSchedule;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
import com.enhantec.framework.security.common.model.EHUser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobManager implements DisposableBean, CommandLineRunner {

    private final EHJobDefinitionService ehJobDefinitionService;

    private final EHJobScheduleService ehJobScheduleService;

    private final ConcurrentHashMap<String, ScheduledFuture> scheduledJobs = new ConcurrentHashMap();

    private final TaskScheduler taskScheduler;

    @Override
    public void run(String... args){

        Logger logger = LoggerFactory.getLogger(this.getClass());
        // 初始化:加载enabled的定时任务
        List<EHJobSchedule> jobScheduleList = ehJobScheduleService.list(Wrappers.lambdaQuery(EHJobSchedule.class).eq(EHJobSchedule::isEnabled,true));
        if (CollectionUtils.isNotEmpty(jobScheduleList)) {
            for (EHJobSchedule jobSchedule : jobScheduleList) {
                EHJobDefinition jobDefinition = ehJobDefinitionService.getById(jobSchedule.getJobDefId());
                ScheduledFuture scheduledFuture = this.taskScheduler.schedule(new JobRunner(jobDefinition), new CronTrigger(jobSchedule.getCronExpression()));
                scheduledJobs.put(jobSchedule.getId(), scheduledFuture);
            }
        }

        logger.info("定时任务加载完毕...");
    }

    public void stopJob(String jobScheduleId) {
        if (jobScheduleId != null) {
            if(scheduledJobs.containsKey(jobScheduleId)) {
                 scheduledJobs.get(jobScheduleId).cancel(true);
                this.scheduledJobs.remove(jobScheduleId);
            }

            val updateWrapper = Wrappers.lambdaUpdate(EHJobSchedule.class).set(EHJobSchedule::isEnabled,false)
                    .eq(EHJobSchedule::getId,jobScheduleId);
            ehJobScheduleService.update(null, updateWrapper);
        }
    }

    public void runJob(String jobScheduleId){

        if(jobScheduleId != null){

            //如果发现已有存在的job还在运行先删除。
            if(scheduledJobs.containsKey(jobScheduleId)) {
                scheduledJobs.get(jobScheduleId).cancel(true);
                this.scheduledJobs.remove(jobScheduleId);
            }

            EHJobSchedule jobSchedule = ehJobScheduleService.getById(jobScheduleId);
            EHJobDefinition jobDefinition = ehJobDefinitionService.getById(jobSchedule.getJobDefId());
            ehJobScheduleService.saveOrUpdate(jobSchedule.toBuilder().enabled(true).build());
            ScheduledFuture scheduledFuture = this.taskScheduler.schedule(new JobRunner(jobDefinition), new CronTrigger(jobSchedule.getCronExpression()));
            scheduledJobs.put(jobScheduleId, scheduledFuture);

        }

    }


    @Override
    public void destroy() {

        for (ScheduledFuture job : this.scheduledJobs.values()) {
            job.cancel(true);
        }

        this.scheduledJobs.clear();
    }

}
