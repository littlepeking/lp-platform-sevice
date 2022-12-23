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



package com.enhantec.framework.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.model.EHUser;
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

    Page<Map<String,Object>> getRolePageData(Page<Map<String,Object>> page, QueryWrapper qw,String languageCode);

    Page<Map<String, Object>> getUserRolePageData(Page<Map<String, Object>> page, QueryWrapper qw, String languageCode);

}
