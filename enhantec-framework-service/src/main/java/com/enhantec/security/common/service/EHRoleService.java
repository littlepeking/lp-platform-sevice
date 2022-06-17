package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.EHRoleMapper;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.EHOrganization;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.EHUserRole;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
public interface EHRoleService extends IService<EHRole> {

    List<EHRole> findAll();

    List<EHRole> findByUserId(String userId);

    List<EHRole> findByOrgId(String orgId);

    List<EHRole> findByUsername(String username);

    List<EHRole> findByOrgIdAndUsername(String orgId, String username);

    List<EHRole> findByOrgIdAndUserId(String orgId, String userId);

    EHRole createOrUpdate(EHRole role);

    void delete(String roleId);

    EHUser assignRolesToUser(String userId, List<String> roleNames);

    EHUser revokeRolesFromUser( String userId, List<String> roleIds);

    Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);

    Page<Map<String, Object>> getUserRolePageData(Page<Map<String, Object>> page, QueryWrapper qw);

}
