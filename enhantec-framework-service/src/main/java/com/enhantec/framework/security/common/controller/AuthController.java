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



package com.enhantec.framework.security.common.controller;

import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHUserDetailsService;
import com.enhantec.framework.security.common.service.JWTCacheService;
import com.enhantec.framework.security.core.jwt.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTCacheService jwtCacheService;

    private final JWTTokenProvider tokenProvider;

    private final EHUserDetailsService ehUserDetailsService;


    @PostMapping("/logout")
    public void logout(Authentication authentication, HttpServletRequest servletRequest){

        String jwt = tokenProvider.resolveToken(servletRequest);
        EHUser user = ehUserDetailsService.getUserInfo(authentication.getName());
        jwtCacheService.removeToken(jwt);
        jwtCacheService.removeUserToken(user.getId());

    }

    public Object getCurrentUser(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication();
        return ehUserDetailsService.getUserInfo(authentication.getName());
    }



}
