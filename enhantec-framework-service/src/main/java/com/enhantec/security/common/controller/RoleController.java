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

    @GetMapping("/findAll")
    public List<EHRole> findAll() {
        return ehRoleService.findAll();
    }


    @GetMapping("/findByOrgId/{orgId}")
    public List<EHRole> findByOrgId(@PathVariable @NotNull String orgId) {
        return ehRoleService.findByOrgId(orgId);
    }

    @GetMapping("/findByUsername/{username}")
    public List<EHRole> findByUsername(@PathVariable @NotNull String username) {
        return ehRoleService.findByUsername(username);
    }


    @GetMapping("/findByOrgIdAndUsername/{orgId}/{username}")
    public List<EHRole> findByOrgIdAndUsername(@PathVariable @NotNull String orgId, @PathVariable @NotNull String username) {
        return ehRoleService.findByOrgIdAndUsername(orgId, username);
    }


    @GetMapping("/findByOrgIdAndUserId/{orgId}/{userId}")
    public List<EHRole> findByOrgIdAndUserId(@PathVariable @NotNull String orgId, @PathVariable @NotNull String userId) {
        return ehRoleService.findByOrgIdAndUserId(orgId, userId);
    }


    @PostMapping("/createOrUpdate")
    public EHRole createOrUpdate(@Valid @RequestBody RoleDTO roleDTO) {

        EHRole role = EHRole.builder()
                .id(roleDTO.getId())
                .orgId(roleDTO.getOrgId())
                .roleName(roleDTO.getRoleName())
                .displayName(roleDTO.getDisplayName()).build();

        return ehRoleService.createOrUpdate(role);

    }

    @DeleteMapping("/{roleId}")
    public void delete(@PathVariable @NotNull String roleId) {
        ehRoleService.delete(roleId);
    }


    @PostMapping("/assignToUser")
    public EHUser assignToUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.assignToUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
    }


    @PostMapping("/revokeFromUser")
    public EHUser revokeFromUser(@Valid @RequestBody UserRolesDTO userRolesDTO) {
        return ehRoleService.revokeFromUser(userRolesDTO.getUserId(), userRolesDTO.getRoleIds());
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