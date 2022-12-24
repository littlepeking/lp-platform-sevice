package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.enhantec.framework.common.model.EHBaseModel;
import com.enhantec.framework.common.model.EHVersionModel;
import lombok.Data;

/**
 * 
 * @TableName eh_job
 */
@TableName(value ="eh_job")
@Data
public class EHJobModel extends EHVersionModel {
    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String remark;

    /**
     * 
     */
    private String beanName;

    /**
     * 
     */
    private String methodName;

    /**
     * 
     */
    private String methodParams;

    /**
     * 
     */
    private String cronExpression;

    /**
     * 
     */
    private Integer enabled;

}