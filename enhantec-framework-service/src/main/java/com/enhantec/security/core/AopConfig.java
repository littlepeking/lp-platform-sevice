package com.enhantec.security.core;

import com.enhantec.security.common.service.RoleHierarchyService;
import com.enhantec.security.core.aspect.RoleHierarchyReloadAspect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@RequiredArgsConstructor
@EnableAspectJAutoProxy
@Configuration
@Slf4j
public class AopConfig {

    private final RoleHierarchyImpl roleHierarchy;
    private final RoleHierarchyService roleHierarchyService;


    @Bean
    public RoleHierarchyReloadAspect roleHierarchyReloadAspect(){
        return new RoleHierarchyReloadAspect(roleHierarchy,roleHierarchyService);
    }
}
