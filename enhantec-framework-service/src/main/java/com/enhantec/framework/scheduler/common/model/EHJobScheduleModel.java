package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

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

    @TableField("ORG_ID")
    private String orgId;

    private String description;

    @TableField(value = "JOB_DEF_ID", updateStrategy = FieldStrategy.IGNORED)
    private String jobDefId;

    @TableField(value = "JOB_ORG_IDS")
    private String jobOrgIds;//逗号分开

    @TableField(value = "JOB_PARAMS")
    private String jobParams;

    @TableField(value = "CRON_EXPRESSION")
    private String cronExpression;

    private boolean enabled;

}