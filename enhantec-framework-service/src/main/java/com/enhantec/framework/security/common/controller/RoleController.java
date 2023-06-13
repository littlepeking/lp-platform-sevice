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



package com.enhantec.framework.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.security.common.dto.RoleDTO;
import com.enhantec.framework.security.common.dto.UserRolesDTO;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security/role")
@RequiredArgsConstructor
public class RoleController {

    private final EHRoleService ehRoleService;

    @GetMapping("/findAll")
    public List<EHRole> findAll() {
        return ehRoleService.findAll();
    }


    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @GetMapping("/findById/{id}")
    public EHRole findById(@NotNull @PathVariable String id){
        return ehRoleService.getById(id);
    }

    @PostMapping(value="/findByOrgId")
    public List<EHRole> findByOrgId(@RequestBody Map<String,String> map){
        if (map.get("orgId")!=null) {
            return ehRoleService.findByOrgId(map.get("orgId"));
        } else {
            throw new EHApplicationException("s-org-idCannotBeNull");
        }
    }

    @GetMapping("/findByUsername/{username}")
    public List<EHRole> findByUsername(@PathVariable @NotNull String username) {
        return ehRoleService.findByUsername(username);
    }


    @GetMapping("/findByOrgIdAndUsername/{orgId}/{username}")
    public List<EHRole> findByOrgIdAndUsername(@PathVariable @NotNull String orgId, @PathVariable @NotNull String username) {
        return ehRoleService.findByOrgIdAndUsername(orgId, username, false);
    }


    @GetMapping("/findByOrgIdAndUserId/{orgId}/{userId}")
    public List<EHRole> findByOrgIdAndUserId(@PathVariable @NotNull String orgId, @PathVariable @NotNull String userId) {
        return ehRoleService.findByOrgIdAndUserId(orgId, userId, false);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @PostMapping("/createOrUpdate")
    public EHRole createOrUpdate(@Valid @RequestBody RoleDTO roleDTO) {

        EHRole role = EHRole.builder()
                .id(roleDTO.getId())
                .orgId(roleDTO.getOrgId())
                .roleName(roleDTO.getRoleName())
                .displayName(roleDTO.getDisplayName())
                .version(roleDTO.getVersion()).build();

        return ehRoleService.createOrUpdate(role);

    }
    @PreAuthorize("hasAnyAuthority('SECURITY_ROLE')")
    @DeleteMapping("")
    public void delete(@RequestBody @NotNull String id) {
        ehRoleService.delete(id);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/assignToUser")
    public EHUser assignToUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.assignToUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/revokeFromUser")
    public EHUser revokeFromUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.revokeFromUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = ehRoleService.getRolePageData(pageInfo, queryWrapper, EHContextHelper.getLanguageCode());

        //DataFormatHelper.formatPageData(res);

        return res;

    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/queryUserRoleByPage")
    public Page<Map<String, Object>> queryUserRoleByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = ehRoleService.getUserRolePageData(pageInfo, queryWrapper, EHContextHelper.getLanguageCode());

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}
