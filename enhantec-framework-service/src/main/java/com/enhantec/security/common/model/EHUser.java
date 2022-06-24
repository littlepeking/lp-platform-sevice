package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.security.core.enums.AuthType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@SuperBuilder
@With
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_USER")
public class EHUser extends EHBaseModel implements UserDetails, Serializable {

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
    private Collection<? extends GrantedAuthority> authorities;

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