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



package com.enhantec.framework.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.security.common.dto.OrgPermissionsDTO;
import com.enhantec.framework.security.common.dto.PermissionDTO;
import com.enhantec.framework.security.common.dto.PermissionOrgsDTO;
import com.enhantec.framework.security.common.dto.RolePermissionsDTO;
import com.enhantec.framework.security.common.model.EHOrganization;
import com.enhantec.framework.security.common.model.EHPermission;
import com.enhantec.framework.security.common.service.EHOrganizationService;
import com.enhantec.framework.security.common.service.EHPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final EHPermissionService permissionService;

    private final EHOrganizationService organizationService;

    @GetMapping("/findAll")
    public List<EHPermission> findAll(){
        return permissionService.findAll(false);
    }


    @GetMapping("/findByOrgId/{orgId}")
    public List<EHPermission> findByOrgId(@PathVariable @NotNull String orgId){
        return permissionService.findByOrgId(orgId);
    }

    @GetMapping("/findByRoleId/{roleId}")
    public List<EHPermission> findByRoleId(@PathVariable @NotNull String roleId){
        return permissionService.findByRoleId(roleId);
    }
    @PreAuthorize("hasAnyAuthority('SECURITY_PERMISSION')")
    @GetMapping("/buildTree")
    public List<EHPermission> buildTree(){
        return permissionService.rebuildPermissionTree();
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @GetMapping("/buildTreeByOrgId")
    public List<EHPermission> buildTreeByOrgId(@RequestParam @NotNull String orgId){
        return permissionService.rebuildOrgPermissionTree(orgId);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @GetMapping("/buildTreeByRoleId")
    public List<EHPermission> buildTreeByRoleId(@RequestParam @NotNull String roleId){
        return permissionService.rebuildRolePermissionTree(roleId);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @PostMapping("/createOrUpdate")
    public EHPermission createOrUpdate(@Valid @RequestBody PermissionDTO permissionDTO){

        EHPermission permission = EHPermission.builder()
                .id(permissionDTO.getId())
                .type(permissionDTO.getType())
                .parentId(permissionDTO.getParentId())
                .displayName(permissionDTO.getDisplayName())
                .authority(permissionDTO.getAuthority())
                .build();

        return permissionService.createOrUpdate(permission);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_PERMISSION')")
    @DeleteMapping("")
    public void delete(@RequestBody @NotNull List<String> permissionIds){
        permissionService.deleteByIds(permissionIds);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @PostMapping("/updateOrgPermissions")
    public List<EHPermission> updateOrgPermissions(@Valid @RequestBody OrgPermissionsDTO orgPermissionsDTO){
        permissionService.updateOrgPermissions(orgPermissionsDTO.getOrgId(),orgPermissionsDTO.getPermissionIds());
        return permissionService.rebuildOrgPermissionTree(orgPermissionsDTO.getOrgId());
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_PERMISSION')")
    @PostMapping("/updatePermissionOrgs")
    public List<EHOrganization> updatePermissionOrgs(@Valid @RequestBody PermissionOrgsDTO permissionOrgsDTO){
        permissionService.updatePermissionOrgs(permissionOrgsDTO.getPermissionId(),permissionOrgsDTO.getOrgIds());
        return organizationService.buildOrgTreeByPermId(permissionOrgsDTO.getPermissionId());
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @PostMapping("/updateRolePermissions")
    public List<EHPermission> updateRolePermissions(@Valid @RequestBody RolePermissionsDTO rolePermissionDTO){
        permissionService.updateRolePermissions(rolePermissionDTO.getRoleId(),rolePermissionDTO.getPermissionIds());
        return permissionService.rebuildRolePermissionTree(rolePermissionDTO.getRoleId());
    }


    @Deprecated
    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = permissionService.getPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }

    @Deprecated
    @PostMapping("/queryRolePermissionsByPage")
    public Page<Map<String, Object>> queryRolePermissionsByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = permissionService.getRolePermissionPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }

}
