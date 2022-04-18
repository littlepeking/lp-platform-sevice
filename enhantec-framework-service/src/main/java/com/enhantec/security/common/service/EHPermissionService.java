package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.*;
import com.enhantec.security.common.model.*;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class EHPermissionService extends ServiceImpl<EHPermissionMapper, EHPermission> {

    private final EHRoleMapper roleMapper;

    private final EHPermissionMapper permissionMapper;

    private final EHRolePermissionMapper rolePermissionMapper;

    public EHPermission createPermission(String permissionName, String displayName) {

        if(!permissionName.equals(permissionName.toUpperCase()))
            throw new EHApplicationException("permission name must be upper case.");

        EHPermission permission = getOne(Wrappers.lambdaQuery(EHPermission.class).eq(EHPermission::getAuthority, permissionName));

        if (permission != null) {
            throw new EHApplicationException("permission name '" + permissionName + "' is already exist.");
        } else {
            EHPermission permToSave = EHPermission.builder().authority(permissionName).displayName(displayName).build();
            save(permToSave);

            return permToSave;
        }

    }


    public List<EHPermission> findByRole(String roleName) {
        val rolePermissionLambdaQueryWrapper = Wrappers.lambdaQuery(EHRolePermission.class).eq(EHRolePermission::getRoleName, roleName);

        List<EHRolePermission> rolePermissionList = rolePermissionMapper.selectList(rolePermissionLambdaQueryWrapper);

        Set<String> permissionSet = rolePermissionList.stream().map(EHRolePermission::getAuthority).collect(Collectors.toSet());

        if (permissionSet.size() > 0 && permissionSet.size() > 0) {
            List<EHPermission> permissionList = permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class).in(EHPermission::getAuthority, permissionSet));
            return permissionList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    @ReloadRoleHierarchy
    public EHRole assignPermToRole(String roleName, List<String> permissions) {

        EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getRoleName, roleName));

        if(role==null) throw new EHApplicationException("role name "+ roleName +" is not exist.");

        if (permissions!=null && permissions.size() > 0) {

            permissions.forEach(perm -> {

                if(!perm.equals(perm.toUpperCase()))
                    throw new EHApplicationException("Permission name must be upper case.");


                val storedPermName = perm.trim();

                EHPermission permission = permissionMapper.selectOne(Wrappers.lambdaQuery(EHPermission.class).eq(EHPermission::getAuthority, storedPermName));

                Optional.ofNullable(permission).ifPresent(
                        (p) -> {
                            EHRolePermission rolePermission = rolePermissionMapper.selectOne(Wrappers.lambdaQuery(EHRolePermission.class)
                                    .eq(EHRolePermission::getRoleName, roleName)
                                    .eq(EHRolePermission::getAuthority, storedPermName));

                            if (rolePermission == null) {
                                rolePermissionMapper.insert(EHRolePermission.builder().roleName(roleName).authority(storedPermName).build()
                                );
                            }
                        }
                );
            });

        }

        val permissionList = findByRole(roleName);

        role.setPermissions(permissionList);

        return role;


    }
}
