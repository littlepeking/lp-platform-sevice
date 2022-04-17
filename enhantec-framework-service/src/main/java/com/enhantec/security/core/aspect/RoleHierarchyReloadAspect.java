package com.enhantec.security.core.aspect;

import com.enhantec.security.common.services.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class RoleHierarchyReloadAspect {

    private final RoleHierarchyImpl roleHierarchy;

    private final RoleHierarchyService roleHierarchyService;

    @Pointcut("execution(* com.enhantec.*.*(..))")
    public void applicationPackagePointCut(){}

    @AfterReturning("applicationPackagePointCut() && @annotation(com.enhantec.security.core.annotation.ReloadRoleHierarchy)")
    public void reloadRoleHierarchy(){

        //TODO: multiple servers roles reload, currently only work for single server
        val roleHierarchyExpression = roleHierarchyService.getRoleHierarchyExpression();

        roleHierarchy.setHierarchy(roleHierarchyExpression);

        log.debug("Role hierarchy reloaded");

    }

}
