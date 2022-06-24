package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.service.impl.EHBaseServiceImpl;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.*;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EHRoleServiceImpl extends EHBaseServiceImpl<EHRoleMapper, EHRole> implements EHRoleService {

    private final EHRoleMapper roleMapper;

    private final EHUserMapper userMapper;

    private final EHUserRoleMapper userRoleMapper;

    public EHRole createOrUpdate(EHRole role) {

        if (StringUtils.isNotEmpty(role.getId())) {

            EHRole existRole = baseMapper.selectById(role.getId());

            if (!existRole.getRoleName().equals(role.getRoleName())) {
                validRoleName(role);
            }

        } else {
            validRoleName(role);
        }

        return saveOrUpdateAndRetE(role);
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

        if (role == null) throw new EHApplicationException("s-role-roleIdNotExist",roleId);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getRoleId, roleId));

        if (!CollectionUtils.isEmpty(userRoleList)) {
            List<String> userIds = userRoleList.stream().map(ur -> ur.getUserId()).collect(Collectors.toList());
            List<EHUser> users = userMapper.selectList(Wrappers.lambdaQuery(EHUser.class).in(EHUser::getId, userIds));

            throw new EHApplicationException("s-role-roleUsedByUser", role.getRoleName(), users.stream().map(u -> u.getUsername()).collect(Collectors.joining(",")));
        }
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

        if (user == null) throw new EHApplicationException("s-usr-usernameNotFound");

        return findByUserId(user.getId());
    }

    public List<EHRole> findByOrgIdAndUsername(String orgId, String username) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user == null) throw new EHApplicationException("s-usr-usernameNotFound");

        return findByOrgIdAndUserId(orgId, user.getId());
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

        user.setAuthorities(roleList);

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
