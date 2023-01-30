/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
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
@DS(DSConstants.DS_MASTER)
public class JobManager implements DisposableBean, CommandLineRunner {

    private final EHJobDefinitionService ehJobDefinitionService;

    private final EHJobScheduleService ehJobScheduleService;

    private final ConcurrentHashMap<String, ScheduledFuture> scheduledJobs = new ConcurrentHashMap();

    private final TaskScheduler taskScheduler;

    @Override
    public void run(String... args){

        Logger logger = LoggerFactory.getLogger(this.getClass());
        // 初始化:加载enabled的定时任务
        logger.info("开始加载已启用的定时任务...");
        List<EHJobScheduleModel> jobScheduleList = ehJobScheduleService.list(Wrappers.lambdaQuery(EHJobScheduleModel.class).eq(EHJobScheduleModel::isEnabled,true));
        if (CollectionUtils.isNotEmpty(jobScheduleList)) {
            for (EHJobScheduleModel jobSchedule : jobScheduleList) {
                EHJobDefinitionModel jobDefinition = ehJobDefinitionService.getById(jobSchedule.getJobDefId());
                ScheduledFuture scheduledFuture = this.taskScheduler.schedule(new JobRunner(jobDefinition, jobSchedule), new CronTrigger(jobSchedule.getCronExpression()));
                scheduledJobs.put(jobSchedule.getId(), scheduledFuture);
            }
        }

        logger.info("定时任务加载完毕。");
    }

    public void saveJob(EHJobDefinitionModel jobDefinition) {
        if (jobDefinition != null) {
            if(jobDefinition.getId()!=null){
              var jobScheduleList =  ehJobScheduleService.list(Wrappers.lambdaQuery(EHJobScheduleModel.class).eq(EHJobScheduleModel::getJobDefId, jobDefinition.getId()));

              if(jobScheduleList!=null) {
                  for (EHJobScheduleModel jobSchedule : jobScheduleList) {
                     if(scheduledJobs.containsKey(jobSchedule.getId()))
                              throw new EHApplicationException("s-job-stopScheduleBeforeSaveJob");
                  }
              }

            }
            ehJobDefinitionService.save(jobDefinition);
        }
    }

    public void removeJob(String jobId) {

        long jobScheduleCount = ehJobScheduleService.count(Wrappers.lambdaQuery(EHJobScheduleModel.class).eq(EHJobScheduleModel::getJobDefId,jobId));

        if(jobScheduleCount > 0){
            throw new EHApplicationException("s-job-deleteScheduleBeforeDeleteJob");
        } else {
            ehJobDefinitionService.removeById(jobId);
        }
    }


    public void saveSchedule(EHJobScheduleModel jobSchedule) {
        if (jobSchedule != null) {
            ehJobScheduleService.save(jobSchedule);
        }
    }

    public void removeSchedule(String schedulerId) {

        if(scheduledJobs.containsKey(schedulerId)) {
            throw new EHApplicationException("s-schedule-stopScheduleBeforeDelete");
        }

        ehJobScheduleService.removeById(schedulerId);

    }


    public void stopSchedule(String jobScheduleId) {
        if (jobScheduleId != null) {
            if(scheduledJobs.containsKey(jobScheduleId)) {
                 scheduledJobs.get(jobScheduleId).cancel(true);
                this.scheduledJobs.remove(jobScheduleId);
            }

            val updateWrapper = Wrappers.lambdaUpdate(EHJobScheduleModel.class).set(EHJobScheduleModel::isEnabled,false)
                    .eq(EHJobScheduleModel::getId,jobScheduleId);
            ehJobScheduleService.update(null, updateWrapper);
        }
    }

    public void runSchedule(String jobScheduleId){

        if(jobScheduleId != null){

            //如果发现已有存在的job还在运行先删除。
            if(scheduledJobs.containsKey(jobScheduleId)) {
                scheduledJobs.get(jobScheduleId).cancel(true);
                this.scheduledJobs.remove(jobScheduleId);
            }

            EHJobScheduleModel jobSchedule = ehJobScheduleService.getById(jobScheduleId);
            EHJobDefinitionModel jobDefinition = ehJobDefinitionService.getById(jobSchedule.getJobDefId());
            ehJobScheduleService.saveOrUpdate(jobSchedule.toBuilder().enabled(true).build());
            ScheduledFuture scheduledFuture = this.taskScheduler.schedule(new JobRunner(jobDefinition, jobSchedule), new CronTrigger(jobSchedule.getCronExpression()));
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
