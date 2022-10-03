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



package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.model.EHRole;
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
