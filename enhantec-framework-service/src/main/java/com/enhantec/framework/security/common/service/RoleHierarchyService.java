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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.mapper.EHRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleHierarchyService {

    private final EHRoleMapper roleMapper;

    private final EHPermissionService permissionService;

    public String getRoleHierarchyExpression(){

            List<EHRole> roles = roleMapper.selectList(Wrappers.lambdaQuery(EHRole.class));

        return roles.stream().flatMap(role -> permissionService.findByRoleIdWithoutTranslate(role.getId()).stream().map(
                ehPermission -> role.getAuthority() + " > " + ehPermission.getAuthority() )).collect(Collectors.joining(
                "\n")
        );

    }

}
