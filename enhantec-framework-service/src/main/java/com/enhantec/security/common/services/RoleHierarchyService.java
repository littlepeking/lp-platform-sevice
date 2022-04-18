package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.mappers.EHRoleMapper;
import com.enhantec.security.common.models.EHRole;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

        return roles.stream().flatMap(role -> permissionService.findByRole(role.getRoleName()).stream().map(
                ehPermission -> role.getRoleName() + " > " + ehPermission.getAuthority() )).collect(Collectors.joining(
                "\n")
        );

    }

}
