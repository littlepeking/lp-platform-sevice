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



package com.enhantec.framework.security.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.security.common.model.EHUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Authenticate a user from the database.
 */
@Service
@RequiredArgsConstructor
@DS("admin")
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
public class EHUserDetailsService implements UserDetailsService {

    private final EHUserService userService;

    private final EHRoleService roleService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpServletRequest request;

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
            //Set all roles to user in login url Only, all other url should based on orgId to differentiate and load roles. It already implemented in JWTFilter.
            user.setRoles(roleService.findByUsername(user.getUsername()));
//            String orgId = request.getHeader("orgId");
//            if(StringUtils.hasText(orgId)) {
//                user.setRoles(roleService.findByOrgIdAndUsername(orgId, user.getUsername(), true));
//            }else {
//                user.setRoles(Collections.emptyList());
//            }

        }
            return user;
    }

}
