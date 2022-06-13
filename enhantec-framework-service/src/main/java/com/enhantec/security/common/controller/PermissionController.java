package com.enhantec.security.common.controller;

import com.enhantec.security.common.dtos.*;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.service.EHPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    @PostMapping("/revokePermissionsaFromRole")
    public EHRole revokePermissionsaFromRole(@Valid @RequestBody RolePermissionDTO rolePermissionDTO){
        return permissionService.assignPermToRole(rolePermissionDTO.getRoleId(),rolePermissionDTO.getPermissionIds());
    }

    @GetMapping("/getPermissionsByRole")
    public List<EHPermission> getPermissionsByRole(@Valid @NotNull String roleId){
        return permissionService.findByRoleId(roleId);
    }

}
