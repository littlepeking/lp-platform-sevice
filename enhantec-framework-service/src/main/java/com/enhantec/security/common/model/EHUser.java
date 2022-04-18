package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.security.core.enums.AuthType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

import java.util.Collection;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_USER")
public class EHUser implements UserDetails, Serializable {

    @TableId
    private String id;

    private String username;

    private AuthType authType;

    private String domainUsername; //ONLY used in AD


    @JsonIgnore
    private String password;

    private boolean enabled;

    @TableField(exist = false)

    @Builder.Default
    private boolean accountNonExpired  = true;
    @TableField(exist = false)
    @Builder.Default
    private boolean accountNonLocked = true;
    @TableField(exist = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @TableField(exist = false)
    private Collection<? extends GrantedAuthority> authorities;

}
