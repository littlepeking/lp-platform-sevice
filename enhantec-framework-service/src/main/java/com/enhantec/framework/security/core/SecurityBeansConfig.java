/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.security.core;

import com.enhantec.framework.config.properties.ApplicationProperties;
import com.enhantec.framework.security.common.service.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(ApplicationProperties.class)
public class SecurityBeansConfig {

    private final RoleHierarchyService roleHierarchyService;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();

        val roleHierarchyExpression = roleHierarchyService.getRoleHierarchyExpression();

        roleHierarchy.setHierarchy(roleHierarchyExpression);

        log.debug("Role hierarchy loaded");

        return roleHierarchy;
    }

}
