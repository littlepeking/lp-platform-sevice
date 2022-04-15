package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.mappers.EHRoleMapper;
import com.enhantec.security.common.mappers.EHUserMapper;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.core.enums.AuthType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component
public class EHUserDetailsService implements UserDetailsService {

    @Autowired
    EHUserService userService;

    @Autowired
    EHRoleService roleService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    final PasswordEncoder passwordEncoder;

    public EHUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EHUser loadUserByUsername(final String userName) {

        EHUser user = getUserInfo(userName);

        if(user==null) throw new UsernameNotFoundException("username is not exists");

        return user;

    }

    public EHUser getUserInfo(final String userName) {

        logger.debug("Authenticating {}", userName);

        String lowercaseLogin = userName.toLowerCase(Locale.ENGLISH);

        EHUser user = userService.getOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,lowercaseLogin));
        if(user==null){
            return null;
        }else {

            user.setAuthorities(roleService.findByUsername(user.getUsername()));

        }
            return user;
    }

     public EHUser createDomainUser(final String userName, final String domainUserName){

         userService.save(EHUser.builder().username(userName).domainUsername(domainUserName).build());

        return new EHUser(userName, AuthType.LDAP,domainUserName,"",true,true,true,true,new ArrayList<>());
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String subject) {

        //  if(subject.equals("cn=john"))
        return Arrays.stream(new String[]{"admin"}).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
