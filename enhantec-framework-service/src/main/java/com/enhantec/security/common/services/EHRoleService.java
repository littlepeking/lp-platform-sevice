package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mappers.EHUserMapper;
import com.enhantec.security.common.mappers.EHUserRoleMapper;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.mappers.EHRoleMapper;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.models.EHUserRole;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.Watchable;
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

        boolean isUserExists = userMapper.exists(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        if(!isUserExists) throw new EHApplicationException("user id is not exist.");

        if (roleNames!=null && roleNames.size() > 0) {

            roleNames.forEach(roleName -> {

                if(!roleName.equals(roleName.toUpperCase()))
                    throw new EHApplicationException("role name must be upper case.");


                val storedRoleName = roleName.trim();

                EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getRoleName, storedRoleName));

                Optional.ofNullable(role).ifPresentOrElse(
                        (r) -> {
                            boolean isExist = userRoleMapper.exists(Wrappers.lambdaQuery(EHUserRole.class)
                                    .eq(EHUserRole::getUserId, userId)
                                    .eq(EHUserRole::getRoleName, storedRoleName));

                            if (!isExist) {
                                userRoleMapper.insert(EHUserRole.builder().userId(userId).roleName(storedRoleName).build()
                                );
                            }
                        },
                        ()-> {throw new EHApplicationException("role name '" + roleName + "' is not exist");}
                );
            });

        }

        EHUser user = userMapper.selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getId, userId));

        val roleList = findByUserId(userId);

        user.setAuthorities(roleList);

        return user;

    }
}
