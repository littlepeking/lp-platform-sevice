package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.model.EHUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Authenticate a user from the database.
 */
@Service
@RequiredArgsConstructor
public class EHUserDetailsService implements UserDetailsService {

    private final EHUserService userService;

    private final EHRoleService roleService;

    private Logger logger = LoggerFactory.getLogger(getClass());


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
