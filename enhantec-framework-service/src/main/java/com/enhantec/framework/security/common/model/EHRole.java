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
import com.enhantec.framework.common.model.EHVersionModel;
import com.enhantec.framework.config.annotations.EHTransField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName("EH_ROLE")
public class EHRole extends EHVersionModel implements GrantedAuthority, Serializable {

    @NotNull
    private String orgId;// ONLY system tables has orgId as all biz transaction table rely on schema name to difference org.

    @NotNull
    private String roleName;

    @NotNull
    @EHTransField
    private String displayName;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return super.getId();
    }


    @TableField(exist = false)
    private Collection<EHPermission> permissions;


}
