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
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.config.properties.ApplicationProperties;
import com.enhantec.framework.security.core.enums.AuthType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_USER")
public class EHUser extends EHVersionModel implements UserDetails, Serializable {

    private String username;

    @TableField("AUTH_TYPE")
    private AuthType authType;

    @TableField("DOMAIN_USERNAME")
    private String domainUsername; //ONLY used in AD


    @TableField("FIRST_NAME")
    private String firstName;

    @TableField("LAST_NAME")
    private String lastName;

    private String remark;

    @JsonIgnore
    @TableField(value = "ORIGINAL_PASSWORD", exist = false)
    private String originalPassword;

    @JsonIgnore
    private String password;

    @TableField("PASSWORD_CHANGED_TIME")
    private LocalDateTime passwordChangedTime;

    @TableField("API_KEY")
    private String apiKey;

    //Enable only used when the user need to be deleted, then we can make it disabled to make all existing data is still consistent.
    private boolean enabled;

    @TableField("ACCOUNT_LOCKED")
    private boolean accountLocked;

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

    public boolean isCredentialsNonExpired() {

            //For LDAP user, passwordChangedTime set to null to ignore credentials expiration checking.
            if (this.passwordChangedTime == null) return true;

            LocalDateTime passwordExpiredDateTime = passwordChangedTime.plusDays(EHContextHelper.getBean(ApplicationProperties.class).getSecurity().getPasswordExpiredDays());

            return LocalDateTime.now().isBefore(passwordExpiredDateTime);

    }

}