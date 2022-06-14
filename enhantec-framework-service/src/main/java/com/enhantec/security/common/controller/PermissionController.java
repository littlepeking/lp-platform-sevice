package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dtos.*;
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

    @GetMapping("/getAllPermissions")
    public List<EHPermission> getAllPermissions(){
        return permissionService.findAll();
    }

    @PostMapping("/createPermission")
    public EHPermission createPermission(@Valid @RequestBody PermissionDTO permissionDTO){
        return permissionService.createPermission(permissionDTO);
    }


    @PostMapping("/assignPermissionsToRole")
    public EHRole assignPermissionsToRole(@Valid @RequestBody RolePermissionDTO rolePermissionDTO){
       return permissionService.assignPermToRole(rolePermissionDTO.getRoleId(),rolePermissionDTO.getPermissionIds());
    }

    @PostMapping("/revokePermissionsFromRole")
    public EHRole revokePermissionsFromRole(@Valid @RequestBody RolePermissionDTO rolePermissionDTO){
        return permissionService.revokePermFromRole(rolePermissionDTO.getRoleId(),rolePermissionDTO.getPermissionIds());
    }

    @GetMapping("/getPermissionsByRole/{roleId}")
    public List<EHPermission> getPermissionsByRole(@PathVariable @NotNull String roleId){
        return permissionService.findByRoleId(roleId);
    }


    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        //todo: check if userId is current user, if not, set userId to current UserId if request user is not admin user.

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String, Object>> res = permissionService.getPageData(pageInfo, queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }

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
