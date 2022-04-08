package com.enhantec.security.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EHUser implements UserDetails, Serializable {

    private String username;

    private String loginName; // username show in the

    private String password;

    private boolean enabled;

    private boolean accountNonExpired  = true;

    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;

    List<EHAuthority> authorities;

}
