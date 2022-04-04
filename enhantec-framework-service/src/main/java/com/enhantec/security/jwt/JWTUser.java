package com.enhantec.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author JiJie.LianG
 * @date 2021-04-08 14:33
 */
public class JWTUser extends User {

    @Getter
    private final String domainUserName; // FULLY QUALIFIED AD USER NAME


    public JWTUser(String userName, String password, Collection<? extends GrantedAuthority> authorities,  String domainUserName) {
        super(userName, password, authorities);
        this.domainUserName=domainUserName;
    }

    public JWTUser(String userName, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,  String domainUserName) {
        super(userName, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.domainUserName = domainUserName;
    }

}
