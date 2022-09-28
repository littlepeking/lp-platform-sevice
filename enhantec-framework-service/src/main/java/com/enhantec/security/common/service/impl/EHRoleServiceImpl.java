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



package com.enhantec.security.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.service.impl.EHBaseServiceImpl;
import com.enhantec.common.utils.DBConst;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.EHUserRole;
import com.enhantec.security.common.service.EHPermissionService;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@DS(DBConst.DS_MASTER)
public class EHRoleServiceImpl extends EHBaseServiceImpl<EHRoleMapper, EHRole> implements EHRoleService {

    private final EHRoleMapper roleMapper;

    private final EHUserMapper userMapper;

    private final EHUserRoleMapper userRoleMapper;

    private final EHPermissionService permissionService;

    public EHRole createOrUpdate(EHRole role) {

        if (StringUtils.isNotEmpty(role.getId())) {

            EHRole existRole = baseMapper.selectById(role.getId());

            if (!existRole.getRoleName().equals(role.getRoleName())) {
                validRoleName(role);
            }

        } else {
            validRoleName(role);
        }

        return saveOrUpdateRetE(role);
    }


    private void validRoleName(EHRole role) {

        //check if new role name already used in the org.
        val count = baseMapper.selectCount(Wrappers.lambdaQuery(EHRole.class)
                .eq(EHRole::getRoleName, role.getRoleName())
                .eq(EHRole::getOrgId, role.getOrgId()));
        if (count > 0)
            throw new EHApplicationException("s-role-roleNameExist", role.getRoleName());

    }


    public void delete(String roleId) {

        EHRole role = roleMapper.selectById(roleId);

        if (role == null) throw new EHApplicationException("s-role-roleIdNotExist", roleId);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getRoleId, roleId));

        if (!CollectionUtils.isEmpty(userRoleList)) {
            List<String> userIds = userRoleList.stream().map(ur -> ur.getUserId()).collect(Collectors.toList());
            List<EHUser> users = userMapper.selectList(Wrappers.lambdaQuery(EHUser.class).in(EHUser::getId, userIds));

            throw new EHApplicationException("s-role-roleUsedByUser", role.getRoleName(), users.stream().map(u -> u.getUsername()).collect(Collectors.joining(",")));
        }



        permissionService.updateRolePermissions(roleId, Collections.EMPTY_LIST);

        roleMapper.deleteById(roleId);

    }


    public List<EHRole> findAll() {
        List<EHRole> roleList = getBaseMapper().selectList(Wrappers.lambdaQuery(EHRole.class));
        return roleList;

    }

    public List<EHRole> findByOrgId(String orgId) {

        List<EHRole> roleList = getBaseMapper().selectList(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getOrgId, orgId));

        return roleList;
    }

    public List<EHRole> findByUserId(String userId) {
        return findByUserId(userId, true);
    }

    public List<EHRole> findByUserId(String userId, boolean loadPermissions) {

        val userRoleLambdaQueryWrapper = Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getUserId, userId);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(userRoleLambdaQueryWrapper);

        Set<String> roleSet = userRoleList.stream().map(EHUserRole::getRoleId).collect(Collectors.toSet());

        if (roleSet.size() > 0) {
            List<EHRole> roleList = roleMapper.selectList(Wrappers.lambdaQuery(EHRole.class).in(EHRole::getId, roleSet));

            if (loadPermissions) {
                roleList.stream().forEach(r -> r.setPermissions(permissionService.findByRoleId(r.getId())));
            }

            return roleList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    public List<EHRole> findByOrgIdAndUserId(String orgId, String userId,boolean loadPermissions) {

        List<EHRole> roleList = findByUserId(userId,loadPermissions);

        return roleList.stream().filter(r -> r.getOrgId().equals(orgId))
                .collect(Collectors.toList());
    }


    public List<EHRole> findByUsername(String username) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user == null) throw new EHApplicationException("s-usr-usernameNotFound");

        return findByUserId(user.getId());
    }

    public List<EHRole> findByOrgIdAndUsername(String orgId, String username,boolean loadPermissions) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user == null) throw new EHApplicationException("s-usr-usernameNotFound");

        return findByOrgIdAndUserId(orgId, user.getId(), loadPermissions);
    }

    public EHUser assignToUser(String userId, List<String> roleIds) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if (user == null) throw new EHApplicationException("s-usr-userIdNotFound");

        if (roleIds != null && roleIds.size() > 0) {

            roleIds.forEach(roleId -> {

                EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getId, roleId));

                Optional.ofNullable(role).ifPresent(
                        (r) -> {
                            EHUserRole userRole = userRoleMapper.selectOne(Wrappers.lambdaQuery(EHUserRole.class)
                                    .eq(EHUserRole::getUserId, userId)
                                    .eq(EHUserRole::getRoleId, roleId));

                            if (userRole == null) {
                                userRoleMapper.insert(EHUserRole.builder().userId(userId).roleId(roleId).build());
                            }
                        }
                );
            });

        }

        val roleList = findByUserId(userId);

        user.setRoles(roleList);

        return user;

    }


    public EHUser revokeFromUser(String userId, List<String> roleIds) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if (user == null) throw new EHApplicationException("s-usr-userIdNotFound");

        if (roleIds != null && roleIds.size() > 0) {

            roleIds.forEach(roleId -> {

                userRoleMapper.delete(Wrappers.lambdaQuery(EHUserRole.class)
                        .eq(EHUserRole::getUserId, userId)
                        .eq(EHUserRole::getRoleId, roleId));

            });

        }

        val roleList = findByUserId(userId);

        user.setRoles(roleList);

        return user;

    }

    public Page<Map<String, Object>> getPageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().selectMapsPage(page, qw);

    }

    public Page<Map<String, Object>> getUserRolePageData(Page<Map<String, Object>> page, QueryWrapper qw, String languageCode) {

        return getBaseMapper().queryUserRolePageData(page, qw, languageCode);

    }

}
