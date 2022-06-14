package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dtos.RoleDTO;
import com.enhantec.security.common.dtos.UserRolesDTO;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/api/security/role")
@RequiredArgsConstructor
public class RoleController {

    private final EHRoleService ehRoleService;

    @GetMapping("/getAllRoles")
    public List<EHRole> getAllRoles() {
        return ehRoleService.findAll();
    }


    @GetMapping("/getRolesByOrgId/{orgId}")
    public List<EHRole> getAllRolesByOrgId(@PathVariable @NotNull String orgId) {
        return ehRoleService.findByOrgId(orgId);
    }

    @GetMapping("/getRolesByUsername/{username}")
    public List<EHRole> getRolesByUsername(@PathVariable @NotNull String username) {
        return ehRoleService.findByUsername(username);
    }


    @GetMapping("/getRolesByOrgIdAndUsername/{orgId}/{username}")
    public List<EHRole> getRolesByOrgIdAndUsername(@PathVariable @NotNull String orgId, @PathVariable @NotNull String username) {
        return ehRoleService.findByOrgIdAndUsername(orgId, username);
    }


    @GetMapping("/getRolesByOrgIdAndUsername/{orgId}/{userId}")
    public List<EHRole> getRolesByOrgIdAndUserId(@PathVariable @NotNull String orgId, @PathVariable @NotNull String userId) {
        return ehRoleService.findByOrgIdAndUserId(orgId, userId);
    }


    @PostMapping("/createRole")
    public EHRole createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return ehRoleService.createRole(roleDTO.getOrgId(), roleDTO.getName(), roleDTO.getDisplayName());
    }

    @PostMapping("/assignRolesToUser")
    public EHUser assignRolesToUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.assignRolesToUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }


    @PostMapping("/RevokeRolesFromUser")
    public EHUser revokeRolesToUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.revokeRolesFromUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }


    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = ehRoleService.getPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }

    @PostMapping("/queryUserRoleByPage")
    public Page<Map<String, Object>> queryUserRoleByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = ehRoleService.getUserRolePageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }



}
