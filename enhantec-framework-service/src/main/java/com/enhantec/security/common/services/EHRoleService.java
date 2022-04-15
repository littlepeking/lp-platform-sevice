package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.security.common.mappers.EHUserRoleMapper;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.mappers.EHRoleMapper;
import com.enhantec.security.common.models.EHUserRole;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EHRoleService extends ServiceImpl<EHRoleMapper,EHRole> {

    private final EHRoleMapper roleMapper;

    private final EHUserRoleMapper userRoleMapper;

    @Transactional
    @ReloadRoleHierarchy
    public EHRole createRole(final String roleName) {

        EHRole role = EHRole.builder().roleName(roleName).build();
        super.save(role);

        return role;
    }


    public  List<EHRole> findByUsername(String username) {
        val userRoleLambdaQueryWrapper = Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getUsername, username);

        List<EHUserRole> userRoleList = userRoleMapper.selectList(userRoleLambdaQueryWrapper);

        Set<String> roleSet = userRoleList.stream().map(EHUserRole::getRoleName).collect(Collectors.toSet());

        if (userRoleList.size() > 0 && roleSet.size() > 0) {
            List<EHRole> roleList = roleMapper.selectList(Wrappers.lambdaQuery(EHRole.class).in(EHRole::getRoleName, roleSet));
            return roleList;
        } else {
            return Collections.EMPTY_LIST;
        }

    }
}
