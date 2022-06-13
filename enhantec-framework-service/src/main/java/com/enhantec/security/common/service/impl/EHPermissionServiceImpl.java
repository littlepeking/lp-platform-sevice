package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.dtos.PermissionDTO;
import com.enhantec.security.common.mapper.EHPermissionMapper;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.mapper.EHRolePermissionMapper;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHRolePermission;
import com.enhantec.security.common.service.EHPermissionService;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EHPermissionServiceImpl extends ServiceImpl<EHPermissionMapper, EHPermission> implements EHPermissionService {

    private final EHRoleMapper roleMapper;

    private final EHPermissionMapper permissionMapper;

    private final EHRolePermissionMapper rolePermissionMapper;

    public EHPermission createPermission(PermissionDTO permissionDTO) {

        if(!permissionDTO.getAuthority().equals(permissionDTO.getAuthority().toUpperCase()))
            throw new EHApplicationException("permission must be upper case.");

        EHPermission permission = getOne(Wrappers.lambdaQuery(EHPermission.class)
                .eq(EHPermission::getAuthority,permissionDTO.getAuthority())
                .eq(EHPermission::getOrgId, permissionDTO.getOrgId()));

        if (permission != null) {
            throw new EHApplicationException("permission '" + permission.getDisplayName() + "' is already exist in organization +"+permissionDTO.getOrgId()+"+.");
        } else {
            EHPermission permToSave = EHPermission.builder()
                    .orgId(permissionDTO.getOrgId())
                    .authority(permissionDTO.getAuthority())
                    .displayName(permissionDTO.getDescription()).build();

            save(permToSave);

            return permToSave;
        }

    }

    public List<EHPermission> findAll() {
            List<EHPermission> permissionList = permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class));
            return permissionList;
    }


    public List<EHPermission> findByRoleId(String roleId) {
        val rolePermissionLambdaQueryWrapper = Wrappers.lambdaQuery(EHRolePermission.class).eq(EHRolePermission::getRoleId, roleId);

        List<EHRolePermission> rolePermissionList = rolePermissionMapper.selectList(rolePermissionLambdaQueryWrapper);

        Set<String> permissionIds = rolePermissionList.stream().map(EHRolePermission::getPermissionId).collect(Collectors.toSet());

        if (permissionIds.size() > 0 && permissionIds.size() > 0) {
            List<EHPermission> permissionList = permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class).in(EHPermission::getId, permissionIds));
            return permissionList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    @ReloadRoleHierarchy
    public EHRole assignPermToRole(String roleId, List<String> permissionIds) {

        EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getId, roleId));

        if(role==null) throw new EHApplicationException("role id "+ roleId +" is not exist.");

        if (permissionIds!=null && permissionIds.size() > 0) {

            permissionIds.forEach(permId -> {

                EHPermission permission = permissionMapper.selectOne(Wrappers.lambdaQuery(EHPermission.class).eq(EHPermission::getId, permId));

                Optional.ofNullable(permission).ifPresent(
                        (p) -> {
                            EHRolePermission rolePermission = rolePermissionMapper.selectOne(Wrappers.lambdaQuery(EHRolePermission.class)
                                    .eq(EHRolePermission::getId, roleId)
                                    .eq(EHRolePermission::getPermissionId, permId));

                            if (rolePermission == null) {
                                rolePermissionMapper.insert(EHRolePermission.builder().id(roleId).permissionId(permId).build()
                                );
                            }
                        }
                );
            });

        }

        val permissionList = findByRoleId(roleId);

        role.setPermissions(permissionList);

        return role;


    }


}
