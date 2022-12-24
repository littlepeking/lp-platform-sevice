package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.enhantec.framework.common.model.EHVersionModel;
import lombok.Data;

/**
 * 
 * @TableName eh_job_schedule
 */
@TableName(value ="eh_job_schedule")
@Data
public class EHJobScheduleModel extends EHVersionModel {

    /**
     * 
     */
    private String jobDefId;

    /**
     * 
     */
    private String remark;

    /**
     * 
     */
    private String cronExpression;

    /**
     * 
     */
    private Integer enabled;

}