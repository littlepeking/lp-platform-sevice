package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.enhantec.framework.common.model.EHVersionModel;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @TableName eh_job_schedule
 */
@TableName(value ="EH_JOB_SCHEDULE")
@Data
@SuperBuilder(toBuilder = true)
public class EHJobSchedule extends EHVersionModel {

    /**
     * 
     */
    private String jobDefId;

    /**
     * 
     */
    private String cronExpression;

    /**
     * 
     */
    private boolean enabled;

}