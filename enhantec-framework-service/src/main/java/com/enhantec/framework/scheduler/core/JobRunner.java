/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHLocaleHelper;
import com.enhantec.framework.common.utils.EHTranslationHelper;
import com.enhantec.framework.config.EHRequestAttributes;
import com.enhantec.framework.config.EHRequestContextHolder;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.model.EHJobScheduleModel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;

@AllArgsConstructor
public class JobRunner implements Runnable{

    EHJobDefinitionModel jobDefinitionModel;

    EHJobScheduleModel jobScheduleModel;

    public void run() {

        Logger logger = LoggerFactory.getLogger(this.getClass());

        logger.info("定时任务开始执行 - bean：{}，参数：{}", jobDefinitionModel.getBeanName(), jobScheduleModel.getJobParams());
        long startTime = System.currentTimeMillis();

        try {

            EHBaseJob job = EHContextHelper.getBean(jobDefinitionModel.getBeanName(), EHBaseJob.class);

            String[] params = jobScheduleModel.getJobParams() != null ? jobScheduleModel.getJobParams().split(","):  new String[]{};

            //对于系统标准的Interface job, params应该传入targetUserId

            if(StringUtils.isNotEmpty(jobScheduleModel.getJobOrgIds())){

                String[] orgIds = jobScheduleModel.getJobOrgIds().split(",");

                Arrays.stream(orgIds).forEach(orgId->{
                    try {
                        //create independent request scope for each org
                        RequestContextHolder.setRequestAttributes(new EHRequestAttributes());
                        EHRequestContextHolder ehRequestContextHolder = EHContextHelper.getBean(EHRequestContextHolder.class);

                        //set fields of reqBean
                        //Note: fetching beans from applicationContext is not required. You can autowire these request scope beans into some initialization method and then call it from here. That will also work. In short, standard spring dependency injection will work in similar way.
                        String dataSource = job.getBatchDataSource(orgId,params);
                        ehRequestContextHolder.setDataSource(dataSource);
                        ehRequestContextHolder.setOrgId(orgId);
                        //RequestContextHolder.getRequestAttributes().setAttribute("dataSource", dataSource ,RequestAttributes.SCOPE_REQUEST);
                        job.run(orgId, params);
                    }catch (Exception e){
                        logger.error(EHLocaleHelper.getMsg("s-job-runJobError",orgId, jobDefinitionModel.getName(), ExceptionUtils.getStackTrace(e)));
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });


            }

//            Method method;
//            if (StringUtils.isNotEmpty(jobDefinition.getMethodParams())) {
//                method = target.getClass().getDeclaredMethod(jobDefinition.getMethodName(), String.class);
//            } else {
//                method = target.getClass().getDeclaredMethod(jobDefinition.getMethodName());
//            }
//
//            ReflectionUtils.makeAccessible(method);
//
//            if (StringUtils.isNotEmpty(jobDefinition.getMethodParams())) {
//                method.invoke(target, jobDefinition.getMethodParams());
//            } else {
//                method.invoke(target);
//            }

        } catch (Exception ex) {
            logger.error("定时任务执行异常 - bean：{}，参数：{}\n错误: {}", jobDefinitionModel.getBeanName(), jobScheduleModel.getJobParams(), ex.toString());
        }

        long times = System.currentTimeMillis() - startTime;
        logger.info("定时任务执行结束 - bean：{}，参数：{}，耗时：{} 毫秒", jobDefinitionModel.getBeanName(), jobScheduleModel.getJobParams(), times);
    }

}
