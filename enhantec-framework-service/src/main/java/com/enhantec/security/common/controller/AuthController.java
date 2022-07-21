package com.enhantec.security.common.controller;

import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import com.enhantec.security.common.service.JWTCacheService;
import com.enhantec.security.core.jwt.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author John Wang
 * @since 2022-04-18
 */
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
