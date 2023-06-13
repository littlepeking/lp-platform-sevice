/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.framework.security.common.model.EHPermission;
import com.enhantec.framework.security.common.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(rollbackFor = Exception.class)
public interface EHPermissionService extends IService<EHPermission> {

    List<EHPermission> findAll(boolean withDirectories);

    List<EHPermission> findByOrgId(String orgId);

    List<EHPermission> findByRoleId(String roleId);

    public List<EHPermission> findByRoleIdWithoutTranslate(String roleId);

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
