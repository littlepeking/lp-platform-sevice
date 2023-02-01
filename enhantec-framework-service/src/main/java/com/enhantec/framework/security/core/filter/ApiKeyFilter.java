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



package com.enhantec.framework.security.core.filter;

import com.enhantec.framework.config.EHRequestContextHolder;
import com.enhantec.framework.config.MultiDataSourceConfig;
import com.enhantec.framework.security.Constants;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHRoleService;
import com.enhantec.framework.security.common.service.EHUserService;
import com.enhantec.framework.security.core.auth.EHAuthException;
import com.enhantec.framework.security.core.auth.EHAuthFailureHandler;
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

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final EHAuthFailureHandler authFailureHandler;

    private final EHRoleService roleService;

    private final EHUserService userService;

    private final EHRequestContextHolder ehRequestContextHolder;

    private String API_KEY_HEADER ="ApiKey";

    public String resolveToken(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey)) {
            return apiKey;
        }
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        if (!Constants.authUrl.equals(servletRequest.getRequestURI())) {

            String apiKey = resolveToken(servletRequest);

            if (apiKey != null) {

                try {
                    EHUser user = userService.lambdaQuery().eq(EHUser::getApiKey, apiKey).one();

                    //Loading roles by organization
                    List<EHRole> roleList;
                    String orgId = servletRequest.getHeader("orgId");

                    ehRequestContextHolder.setOrgId(orgId);
                    //框架默认的数据源使用ORGID的数据源,Job或接口程序可根据实际情况进行覆盖。
                    ehRequestContextHolder.setDataSource(MultiDataSourceConfig.DATA_SOURCE_ORG_PREFIX + orgId);

                    if (StringUtils.hasText(orgId)) {
                        roleList = roleService.findByOrgIdAndUserId(orgId, (user.getId()), true);
                    } else {
                        roleList = Collections.emptyList();
                    }

                    //loading authentication
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(user, "", roleList);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (Exception e) {
                    authFailureHandler.onAuthenticationFailure(servletRequest, servletResponse, new EHAuthException("s-auth-apiKeyIsNotValid"));
                    return;
                }

            }

        }


        filterChain.doFilter(servletRequest, servletResponse);

    }


}
