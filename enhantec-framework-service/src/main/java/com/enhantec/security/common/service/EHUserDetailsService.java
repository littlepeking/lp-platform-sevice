package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.model.EHUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

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

}
