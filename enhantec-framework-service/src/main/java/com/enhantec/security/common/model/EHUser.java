/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHVersionModel;
import com.enhantec.security.core.enums.AuthType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

@Data
@SuperBuilder
@With
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_USER")
public class EHUser extends EHVersionModel implements UserDetails, Serializable {

    private String username;

    private AuthType authType;

    private String domainUsername; //ONLY used in AD

    private String firstName;

    private String lastName;

    @JsonIgnore
    @TableField(exist = false)
    private String originalPassword;

    @JsonIgnore
    private String password;

    //Enable only used when the user need to be deleted, then we can make it disabled to make all existing data is still consistent.
    private boolean enabled;

    private boolean accountLocked;
    private boolean credentialsExpired;

    @TableField(exist = false)
    private Collection<? extends GrantedAuthority> roles;

    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() { return roles;}

    @JsonIgnore
    public boolean isAccountNonLocked(){
        return !accountLocked;
    }

    @JsonIgnore
    public boolean isAccountNonExpired(){
        return true;
    }

    @JsonIgnore
    public boolean isCredentialsNonExpired(){
        return !credentialsExpired;
    }
}