package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.enhantec.framework.common.model.EHVersionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @TableName eh_job_schedule
 */
@TableName(value ="EH_JOB_SCHEDULE")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EHJobScheduleModel extends EHVersionModel {

    private String orgId;

    private String description;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String jobDefId;

    private String jobOrgIds;//逗号分开

    private String jobParams;

    private String cronExpression;

    private boolean enabled;

}