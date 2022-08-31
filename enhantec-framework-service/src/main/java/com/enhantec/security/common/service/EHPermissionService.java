package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.security.common.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
public interface EHPermissionService extends IService<EHPermission> {

    List<EHPermission> findAll(boolean withDirectories);

    List<EHPermission> findByOrgId(String orgId);

    List<EHPermission> findByRoleId(String roleId);

    List<EHPermission> rebuildPermissionTree();

    List<EHPermission> rebuildOrgPermissionTree(String orgId);

    List<EHPermission> rebuildRolePermissionTree(String orgId);

    EHPermission createOrUpdate(EHPermission permission);

    void deleteById(String permissionId);

    void deleteByIds(List<String> permissionIds);

    void updateOrgPermissions(String orgId, List<String> updatedPermissionIds);

    void updatePermissionOrgs(String permissionId, List<String> updatedOrgIds);

    void updateRolePermissions(String roleId, List<String> updatedPermissionIds);
    @Deprecated
    Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);
    @Deprecated
    Page<Map<String, Object>> getRolePermissionPageData(Page<Map<String, Object>> page, QueryWrapper qw);

}
