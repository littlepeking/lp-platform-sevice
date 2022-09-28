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

    List<EHPermission> rebuildRolePermissionTree(String roleId);

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
