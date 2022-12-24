/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.scheduler.core;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHVersionModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("EH_JOB")
public class JobDefinition extends EHVersionModel {

    /**
     * ID
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * bean名称
     */
    private String beanName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 方法参数
     */
    private String methodParams;
    /**
     * cron表达式
     */
    private String cronExpression;
    /**
     * 是否启用
     */
    private boolean enabled;
    /**
     * 创建时间
     */
    private LocalDateTime addDate;

    /**
     * 更新时间
     */
    private LocalDateTime editDate;

}
