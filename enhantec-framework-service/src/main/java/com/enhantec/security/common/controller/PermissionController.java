package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dto.*;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.service.EHPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

    @GetMapping("/buildTree")
    public List<EHPermission> buildTree(){
        return permissionService.rebuildPermissionTree();
    }

    @GetMapping("/buildTreeByOrgId")
    public List<EHPermission> buildTreeByOrgId(@RequestParam String orgId){
        return permissionService.rebuildOrgPermissionTree(orgId);
    }

    @GetMapping("/buildTreeByRoleId/{roleId}")
    public EHPermission buildTreeByRoleId(@PathVariable @NotNull String roleId){
        return permissionService.rebuildRolePermissionTree(roleId);
    }

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

    @DeleteMapping("")
    public void delete(@RequestBody @NotNull List<String> permissionIds){
        permissionService.deleteByIds(permissionIds);
    }

    @PostMapping("/updateOrgPermissions")
    public List<EHPermission> updateOrgPermissions(@Valid @RequestBody OrgPermissionsDTO orgPermissionsDTO){
        return permissionService.updateOrgPermissions(orgPermissionsDTO.getOrgId(),orgPermissionsDTO.getPermissionIds());
    }

    @PostMapping("/updateRolePermissions")
    public EHRole updateRolePermissions(@Valid @RequestBody RolePermissionsDTO rolePermissionDTO){
       return permissionService.updateRolePermissions(rolePermissionDTO.getRoleId(),rolePermissionDTO.getPermissionIds());
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
