package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.security.common.mappers.EHPermissionMapper;
import com.enhantec.security.common.mappers.EHRoleMapper;
import com.enhantec.security.common.mappers.EHRolePermissionMapper;
import com.enhantec.security.common.mappers.EHUserRoleMapper;
import com.enhantec.security.common.models.EHPermission;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.models.EHRolePermission;
import com.enhantec.security.common.models.EHUserRole;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EHPermissionService extends ServiceImpl<EHPermissionMapper, EHPermission> {

    private final EHPermissionMapper permissionMapper;

    private final EHRolePermissionMapper rolePermissionMapper;

    private final EHRoleService roleService;

    public Collection<EHPermission> findByRole(String roleName) {
        val rolePermissionLambdaQueryWrapper = Wrappers.lambdaQuery(EHRolePermission.class).eq(EHRolePermission::getRoleName, roleName);

        List<EHRolePermission> userRoleList = rolePermissionMapper.selectList(rolePermissionLambdaQueryWrapper);

        Set<String> authoritySet = userRoleList.stream().map(EHRolePermission::getAuthority).collect(Collectors.toSet());

        if (userRoleList.size() > 0 && authoritySet.size() > 0) {
            List<EHPermission> permissionList = permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class).in(EHPermission::getAuthority, authoritySet));
            return  permissionList;
        }else {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection<EHPermission> findByUsername(String username) {

        List<EHRole> roleList = roleService.findByUsername(username);

        Set<EHPermission> permissionSet = new HashSet<>();

        if (roleList.size() > 0) {

            permissionSet.addAll(roleList.stream().flatMap(role-> findByRole(role.getRoleName()).stream()).collect(Collectors.toSet()));

        }

        return permissionSet;
    }

}
