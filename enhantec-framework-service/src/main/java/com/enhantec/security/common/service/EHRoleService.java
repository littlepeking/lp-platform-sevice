package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.EHUserRole;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class EHRoleService extends ServiceImpl<EHRoleMapper, EHRole> {

    private final EHRoleMapper roleMapper;

    private final EHUserMapper userMapper;

    private final EHUserRoleMapper userRoleMapper;

    public EHRole createRole(String roleName, String displayName) {

        if(!roleName.equals(roleName.toUpperCase()))
            throw new EHApplicationException("role name must be upper case.");

        EHRole role = getOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getRoleName, roleName));

        if (role != null) {
            throw new EHApplicationException("role name '" + roleName + "' is already exist.");
        } else {
            EHRole roleToSave = EHRole.builder().roleName(roleName).displayName(displayName).build();
            save(roleToSave);

            return roleToSave;
        }

    }


    public List<EHRole> findByUserId(String userId) {

        val userRoleLambdaQueryWrapper = Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getUserId, userId);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(userRoleLambdaQueryWrapper);

        Set<String> roleSet = userRoleList.stream().map(EHUserRole::getRoleName).collect(Collectors.toSet());

        if (userRoleList.size() > 0 && roleSet.size() > 0) {
            List<EHRole> roleList = roleMapper.selectList(Wrappers.lambdaQuery(EHRole.class).in(EHRole::getRoleName, roleSet));
            return roleList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }


    public List<EHRole> findByUsername(String username) {

       EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,username));

       if( user == null ) throw  new EHApplicationException("Username "+ username +" is not exists.");

       return findByUserId(user.getId());
    }

    public EHUser assignRolesToUser(String userId, List<String> roleNames) {

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if(user==null) throw new EHApplicationException("user id is not exist.");

        if (roleNames!=null && roleNames.size() > 0) {

            roleNames.forEach(roleName -> {

                if(!roleName.equals(roleName.toUpperCase()))
                    throw new EHApplicationException("role name must be upper case.");


                val storedRoleName = roleName.trim();

                EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getRoleName, storedRoleName));

                Optional.ofNullable(role).ifPresentOrElse(
                        (r) -> {
                            EHUserRole userRole = userRoleMapper.selectOne(Wrappers.lambdaQuery(EHUserRole.class)
                                    .eq(EHUserRole::getUserId, userId)
                                    .eq(EHUserRole::getRoleName, storedRoleName));

                            if (userRole == null) {
                                userRoleMapper.insert(EHUserRole.builder().userId(userId).roleName(storedRoleName).build()
                                );
                            }
                        },
                        ()-> {throw new EHApplicationException("role name '" + roleName + "' is not exist");}
                );
            });

        }

        val roleList = findByUserId(userId);

        user.setAuthorities(roleList);

        return user;

    }
}
