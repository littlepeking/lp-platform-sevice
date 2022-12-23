/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.framework.security.core.jwt;

import com.enhantec.framework.security.Constants;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.service.EHRoleService;
import com.enhantec.framework.security.common.service.EHUserService;
import com.enhantec.framework.security.common.service.JWTCacheService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTTokenProvider tokenProvider;

    private final JwtAuthFailureHandler jwtAuthFailureHandler;

    private final EHRoleService roleService;


    private final EHUserService userService;

    private final JWTCacheService jwtCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        if(!Constants.authUrl.equals(servletRequest.getRequestURI())) {

            //Check if jwt is valid
            String jwt = tokenProvider.resolveToken(servletRequest);


            if (jwt != null) {
                if (StringUtils.hasText(jwt)) {
                    Optional<Claims> claims = this.tokenProvider.getTokenClaims(jwt);
                    if (claims.isPresent()) {
                        //check if jwt is still active in redis
                        if (null == jwtCacheService.getToken(jwt)) {
                            jwtAuthFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, new JwtAuthException("s-auth-userLoginExpired"));
                            return;
                        } else {
                            //refresh redis cache
                            jwtCacheService.addOrRenewToken(jwt, servletRequest.getRemoteAddr());
                            jwtCacheService.addOrRenewUserToken(claims.get().get("userId").toString(), jwt);
                        }

                        //Loading roles by organization
                        List<EHRole> roleList;
                        String orgId = servletRequest.getHeader("orgId");

                        if(StringUtils.hasText(orgId)){
                            roleList = roleService.findByOrgIdAndUserId(orgId, (claims.get().get("userId").toString()),true);

                        }else {
                            roleList = Collections.emptyList();
                        }

                        //loading authentication
                        Authentication authentication =
                                new UsernamePasswordAuthenticationToken(userService.getById(claims.get().get("userId").toString()), "", roleList);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        jwtAuthFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, new JwtAuthException("s-auth-noClaimInfoInJWTToken"));
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }


}
