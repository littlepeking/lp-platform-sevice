package com.enhantec.framework.scheduler.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.enhantec.framework.common.model.EHVersionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName eh_job
 */
@TableName(value ="EH_JOB_DEFINITION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EHJobDefinitionModel extends EHVersionModel {

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String remark;


    @TableField(value = "BEAN_NAME")
    private String beanName;

//    /**
//     *
//     */
//    private String methodName;

//    /**
//     *
//     */
//    private String methodParams;

}