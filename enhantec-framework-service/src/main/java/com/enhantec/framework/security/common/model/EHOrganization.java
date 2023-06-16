/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHTreeModel;
import com.enhantec.framework.config.annotations.EHTransField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 
 * @TableName eh_organization
 */
@TableName(value ="EH_ORGANIZATION")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EHOrganization extends EHTreeModel<EHOrganization> implements Serializable {

    private String code;
    /**
     * 
     */
    @NotNull
    @EHTransField
    private String name;

    @NotNull
    private String connectionStringParams;

    /**
     * 
     */
    @EHTransField
    private String address1;

    /**
     * 
     */
    @EHTransField
    private String address2;

    /**
     * 
     */
    @EHTransField
    private String contact1;

    /**
     * 
     */
    @EHTransField
    private String contact2;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}