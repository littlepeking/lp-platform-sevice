package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_USER")
public class EHUser implements UserDetails, Serializable {

    private String username;

    private String domainUsername; //ONLY used in AD

    @JsonIgnore
    private String password;

    private boolean enabled;

    @TableField(exist = false)
    private boolean accountNonExpired  = true;
    @TableField(exist = false)
    private boolean accountNonLocked = true;
    @TableField(exist = false)
    private boolean credentialsNonExpired = true;

    @TableField(exist = false)
    private Collection<? extends GrantedAuthority> authorities;

}
