/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import com.enhantec.framework.common.utils.EHContextHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;

@Data
public class Job implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(Job.class);

    private JobDefinition jobDefinition;

    private volatile ScheduledFuture<?> scheduledJobInstance;

    public Job(JobDefinition jobDefinition){
        this.jobDefinition = jobDefinition;
    }

    @Override
    public void run() {
        logger.info("定时任务开始执行 - bean：{}，方法：{}，参数：{}", jobDefinition.getBeanName(), jobDefinition.getMethodName(), jobDefinition.getMethodParams());
        long startTime = System.currentTimeMillis();

        try {
            Object target = EHContextHelper.getBean(jobDefinition.getBeanName());

            Method method;
            if (StringUtils.isNotEmpty(jobDefinition.getMethodParams())) {
                method = target.getClass().getDeclaredMethod(jobDefinition.getMethodName(), String.class);
            } else {
                method = target.getClass().getDeclaredMethod(jobDefinition.getMethodName());
            }

            ReflectionUtils.makeAccessible(method);
            if (StringUtils.isNotEmpty(jobDefinition.getMethodParams())) {
                method.invoke(target, jobDefinition.getMethodParams());
            } else {
                method.invoke(target);
            }
        } catch (Exception ex) {
            logger.error(String.format("定时任务执行异常 - bean：%s，方法：%s，参数：%s ", jobDefinition.getBeanName(), jobDefinition.getMethodName(), jobDefinition.getMethodParams()), ex);
        }

        long times = System.currentTimeMillis() - startTime;
        logger.info("定时任务执行结束 - bean：{}，方法：{}，参数：{}，耗时：{} 毫秒", jobDefinition.getBeanName(), jobDefinition.getMethodName(), jobDefinition.getMethodParams(), times);
    }

    public void cancel() {
        if (this.scheduledJobInstance != null) {
            this.scheduledJobInstance.cancel(true);
        }
    }

}
