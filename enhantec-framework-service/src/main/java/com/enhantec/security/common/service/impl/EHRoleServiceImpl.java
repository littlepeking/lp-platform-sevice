package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.EHUserRole;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EHRoleServiceImpl extends ServiceImpl<EHRoleMapper, EHRole> implements EHRoleService {

    private final EHRoleMapper roleMapper;

    private final EHUserMapper userMapper;

    private final EHUserRoleMapper userRoleMapper;

    public EHRole createRole(String orgId, String roleName, String description) {

        if (!roleName.equals(roleName.toUpperCase()))
            throw new EHApplicationException("role name must be upper case.");

        EHRole role = getOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getRoleName, roleName).eq(EHRole::getOrgId, orgId));

        if (role != null) {
            throw new EHApplicationException("role name '" + roleName + "' is already exist.");
        } else {
            EHRole roleToSave = EHRole.builder().orgId(orgId).roleName(roleName).displayName(description).build();
            save(roleToSave);

            return roleToSave;
        }

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

        val userRoleLambdaQueryWrapper = Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getUserId, userId);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(userRoleLambdaQueryWrapper);

        Set<String> roleSet = userRoleList.stream().map(EHUserRole::getRoleId).collect(Collectors.toSet());

        if (userRoleList.size() > 0 && roleSet.size() > 0) {
            List<EHRole> roleList = roleMapper.selectList(Wrappers.lambdaQuery(EHRole.class).in(EHRole::getId, roleSet));
            return roleList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    public List<EHRole> findByOrgIdAndUserId(String orgId, String userId) {

        List<EHRole> roleList = findByUserId(userId);

        return roleList.stream().filter(r -> r.getOrgId().equals(orgId))
                .collect(Collectors.toList());
    }


    public List<EHRole> findByUsername(String username) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user == null) throw new EHApplicationException("Username " + username + " is not exists.");

        return findByUserId(user.getId());
    }

    public List<EHRole> findByOrgIdAndUsername(String orgId, String username) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user == null) throw new EHApplicationException("Username " + username + " is not exists.");

        return findByOrgIdAndUserId(orgId, user.getId());
    }

    public EHUser assignRolesToUser(String userId, List<String> roleIds) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if (user == null) throw new EHApplicationException("user id is not exist.");

        if (roleIds != null && roleIds.size() > 0) {

            roleIds.forEach(roleId -> {

                EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getId, roleId));

                Optional.ofNullable(role).ifPresent(
                        (r) -> {
                            EHUserRole userRole = userRoleMapper.selectOne(Wrappers.lambdaQuery(EHUserRole.class)
                                    .eq(EHUserRole::getUserId, userId)
                                    .eq(EHUserRole::getId, roleId));

                            if (userRole == null) {
                                userRoleMapper.insert(EHUserRole.builder().userId(userId).roleId(roleId).build());
                            }
                        }
                );
            });

        }

        val roleList = findByUserId(userId);

        user.setAuthorities(roleList);

        return user;

    }


    public EHUser revokeRolesFromUser(String userId, List<String> roleIds) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if (user == null) throw new EHApplicationException("user id is not exist.");

        if (roleIds != null && roleIds.size() > 0) {

            roleIds.forEach(roleId -> {

                userRoleMapper.delete(Wrappers.lambdaQuery(EHUserRole.class)
                        .eq(EHUserRole::getUserId, userId)
                        .eq(EHUserRole::getRoleId, roleId));

            });

        }

        val roleList = findByUserId(userId);

        user.setAuthorities(roleList);

        return user;

    }

    public Page<Map<String, Object>> getPageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().selectMapsPage(page, qw);

    }

    public Page<Map<String, Object>> getUserRolePageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().queryUserRolePageData(page, qw);

    }

}
