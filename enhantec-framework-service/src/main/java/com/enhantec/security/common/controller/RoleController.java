package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dto.RoleDTO;
import com.enhantec.security.common.dto.UserRolesDTO;
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

    @GetMapping("/getAll")
    public List<EHRole> getAll() {
        return ehRoleService.findAll();
    }


    @GetMapping("/getByOrgId/{orgId}")
    public List<EHRole> getByOrgId(@PathVariable @NotNull String orgId) {
        return ehRoleService.findByOrgId(orgId);
    }

    @GetMapping("/getByUsername/{username}")
    public List<EHRole> getByUsername(@PathVariable @NotNull String username) {
        return ehRoleService.findByUsername(username);
    }


    @GetMapping("/getByOrgIdAndUsername/{orgId}/{username}")
    public List<EHRole> getByOrgIdAndUsername(@PathVariable @NotNull String orgId, @PathVariable @NotNull String username) {
        return ehRoleService.findByOrgIdAndUsername(orgId, username);
    }


    @GetMapping("/getByOrgIdAndUsername/{orgId}/{userId}")
    public List<EHRole> getByOrgIdAndUserId(@PathVariable @NotNull String orgId, @PathVariable @NotNull String userId) {
        return ehRoleService.findByOrgIdAndUserId(orgId, userId);
    }


    @PostMapping("/createOrUpdate")
    public EHRole createOrUpdate(@Valid @RequestBody RoleDTO roleDTO) {

        EHRole role = EHRole.builder()
                .id(roleDTO.getOrgId())
                .orgId(roleDTO.getOrgId())
                .roleName(roleDTO.getRoleName())
                .displayName(roleDTO.getDisplayName()).build();

        ehRoleService.save(role);

        return role;
    }

    @PostMapping("/deleteRole/{roleId}")
    public void deleteRole(@PathVariable @NotNull String roleId) {
        ehRoleService.delete(roleId);
    }


    @PostMapping("/assignRolesToUser")
    public EHUser assignRolesToUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.assignRolesToUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }


    @PostMapping("/revokeRolesFromUser")
    public EHUser revokeRolesFromUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
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
