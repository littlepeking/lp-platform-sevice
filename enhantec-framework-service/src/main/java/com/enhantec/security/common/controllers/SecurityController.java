package com.enhantec.security.common.controllers;

import com.enhantec.security.common.dtos.*;
import com.enhantec.security.common.models.EHPermission;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.services.EHPermissionService;
import com.enhantec.security.common.services.EHRoleService;
import com.enhantec.security.common.services.EHUserService;
import com.enhantec.security.common.services.EHUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final EHUserDetailsService ehUserDetailsService;

    private final EHUserService userService;

    private final EHRoleService roleService;

    private final EHPermissionService permissionService;

    @PostMapping("/createUser")
    public EHUser createUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO){
        return userService.createUser(userRegisterDTO.getUsername(),userRegisterDTO.getPassword(),userRegisterDTO.getAuth_type());
    }

    @PostMapping("/createRole")
    public EHRole createRole(@Valid @RequestBody RoleDTO roleDTO){
        return roleService.createRole(roleDTO.getRoleName(),roleDTO.getDisplayName());
    }

    @PostMapping("/createPermission")
    public EHPermission createPermission(@Valid @RequestBody PermissionDTO permissionDTO){
        return permissionService.createPermission(permissionDTO.getPermissionName(),permissionDTO.getDisplayName());
    }


    @PostMapping("/assignRolesToUser")
    public EHUser assignRolesToUser(@Valid @RequestBody UserRolesDTO userRolesDTO){
        return roleService.assignRolesToUser(userRolesDTO.getUserId(),userRolesDTO.getRoles());
    }

    @PostMapping("/assignPermissionsToRole")
    public EHRole assignPermissionsToRole(@Valid @RequestBody RolePermissionDTO rolePermissionDTO){
       return permissionService.assignPermToRole(rolePermissionDTO.getRoleName(),rolePermissionDTO.getPermissions());
    }

    @GetMapping("/getRolesByUsername")
    public List<EHRole> getRolesByUsername(@Valid @NotNull String username){
        return roleService.findByUsername(username);
    }

    @GetMapping("/getPermissionsByRole")
    public List<EHPermission> getPermissionsByRole(@Valid @NotNull String roleName){
        return permissionService.findByRole(roleName);
    }


    @GetMapping("/userInfo")
    @PreAuthorize("hasAuthority('USER_READ')")
    public Object getCurrentUser(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication();
        return ehUserDetailsService.getUserInfo(authentication.getName());
    }

    @GetMapping("/userDetail")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public Object getAuthentication(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication();
        return ehUserDetailsService.getUserInfo(authentication.getName());
    }

}
