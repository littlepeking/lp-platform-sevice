package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.service.EHBaseService;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
public interface EHRoleService extends EHBaseService<EHRole> {

    List<EHRole> findAll();

    List<EHRole> findByUserId(String userId);

    List<EHRole> findByUserId(String userId, boolean loadPermissions);

    List<EHRole> findByOrgId(String orgId);

    List<EHRole> findByUsername(String username);

    List<EHRole> findByOrgIdAndUsername(String orgId, String username,boolean loadPermissions);

    List<EHRole> findByOrgIdAndUserId(String orgId, String userId,boolean loadPermissions);

    EHRole createOrUpdate(EHRole role);

    void delete(String roleId);

    EHUser assignToUser(String userId, List<String> roleNames);

    EHUser revokeFromUser(String userId, List<String> roleIds);

    Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);

    Page<Map<String, Object>> getUserRolePageData(Page<Map<String, Object>> page, QueryWrapper qw);

}
