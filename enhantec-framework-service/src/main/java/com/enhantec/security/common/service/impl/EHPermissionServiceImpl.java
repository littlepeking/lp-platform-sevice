package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.dto.PermissionDTO;
import com.enhantec.security.common.enums.PermissionType;
import com.enhantec.security.common.mapper.*;
import com.enhantec.security.common.model.*;
import com.enhantec.security.common.service.EHPermissionService;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EHPermissionServiceImpl extends ServiceImpl<EHPermissionMapper, EHPermission> implements EHPermissionService {

    private final EHRoleMapper roleMapper;

    private final EHPermissionMapper permissionMapper;

    private final EHRolePermissionMapper rolePermissionMapper;

    private final EHOrganizationMapper organizationMapper;

    private final EHOrgPermissionMapper orgPermissionMapper;

    public EHPermission createOrUpdate(EHPermission permission) {

        if (!permission.getAuthority().equals(permission.getAuthority().toUpperCase()))
            throw new EHApplicationException("Permission must be upper case.");

            if (PermissionType.Permission.toString().equals(permission.getType()) &&
                    StringUtils.isEmpty(permission.getAuthority())
            ) {
                throw new EHApplicationException("Permission must provide authority.");
            } else if (PermissionType.Directory.toString().equals(permission.getType()) &&
                    StringUtils.isNotEmpty(permission.getAuthority())
            ) {
                throw new EHApplicationException("Directory should not provide authority.");
            }

            save(permission);

            return permission;
    }


    public void deleteById(String permissionId) {

        EHPermission permission = permissionMapper.selectById(permissionId);

        if (permission == null) throw new EHApplicationException("permissionId " + permissionId + " does not exist.");

        List<EHOrgPermission> orgPermissionList = orgPermissionMapper.selectList(Wrappers.lambdaQuery(EHOrgPermission.class).eq(EHOrgPermission::getPermissionId, permissionId));

        Optional.ofNullable(orgPermissionList).ifPresentOrElse((list) -> {
                    List<String> orgIds = list.stream().map(op -> op.getOrgId()).collect(Collectors.toList());
                    List<EHOrganization> orgs = organizationMapper.selectList(Wrappers.lambdaQuery(EHOrganization.class).in(EHOrganization::getId, orgIds));

                    throw new EHApplicationException("Permission " + permission.getDisplayName() + " still used by organization " + orgs.stream().map(org -> org.getName()).collect(Collectors.joining(","))+".");
                }
                , () ->  permissionMapper.deleteById(permissionId)

        );


    }

    public void deleteByIds(List<String> permissionIds) {
        permissionIds.stream().forEach(p-> deleteById(p));
    }


    public List<EHPermission> findAll(boolean withDirectories) {
        if(withDirectories) {
            return permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class));
        }else {
            return permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class)
                    .eq(EHPermission::getType,PermissionType.Permission));
        }
    }


    public EHPermission rebuildPermissionTree() {
        List<EHPermission> permissionList = findAll(true);
        return buildFullPermissionTree(permissionList,true);

    }

    public EHPermission rebuildOrgPermissionTree(String orgId) {

        List<EHOrgPermission> orgPermissionList = orgPermissionMapper.selectList(Wrappers.lambdaQuery(EHOrgPermission.class)
                .eq(EHOrgPermission::getOrgId, orgId));

        List<EHPermission> permissionList = findAll(true);

        permissionList.stream().forEach(p -> {

            p.setCheckStatus(orgPermissionList.stream().anyMatch(op->op.getPermissionId().equals(p.getId())) ? 1 : 0);

        });

        EHPermission RootDirectory = buildFullPermissionTree(permissionList,false);

        recursivelyCalculateCheckStatus(RootDirectory);

        return RootDirectory;

    }

    public EHPermission rebuildRolePermissionTree(String roleId) {

        EHRole role = roleMapper.selectById(roleId);

        if (role == null) throw new EHApplicationException("Role id " + roleId + " is not exist.");

        List<EHPermission> orgPermissionsList = findByOrgId(role.getOrgId());

        List<EHPermission> fullDirectoryList = permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class)
                .eq(EHPermission::getType,PermissionType.Directory.toString()));

        List<EHPermission>  orgPermsAndFullDirsList = Stream.concat(orgPermissionsList.stream(), fullDirectoryList.stream())
                .collect(Collectors.toList());

        List<EHPermission> rolePermissionsList = findByRoleId(role.getId());

        orgPermsAndFullDirsList.stream().forEach(op -> {

            op.setCheckStatus(rolePermissionsList.stream().anyMatch(rp->op.getId().equals(rp.getId())) ? 1 : 0);

        });

        EHPermission rootDirectory = buildFullPermissionTree(orgPermsAndFullDirsList,false);

        recursivelyCalculateCheckStatus(rootDirectory);

        return rootDirectory;

    }

    private void recursivelyCalculateCheckStatus(EHPermission permission) {

        if (permission.getType().equals(PermissionType.Permission.toString())) {
            return;
        } else {
            //PermissionType = D
            if (permission.getChildren() == null || permission.getChildren().size() == 0) {
                permission.setCheckStatus(1); //if empty folder then mark status as Selected
            } else {
                int status = 0;
                permission.getChildren().forEach(p -> {
                    if (p.getType().equals(PermissionType.Directory)) {
                        recursivelyCalculateCheckStatus(p);
                    }
                });

                permission.setCheckStatus(evaluatePermissionCheckStatus(permission.getChildren()));

            }


        }
    }

    private int evaluatePermissionCheckStatus(List<EHPermission> permissionList) {

        boolean dirContainsUnSelected = permissionList.stream().anyMatch(p -> p.getCheckStatus() == 0);
        boolean dirContainsSelected = permissionList.stream().anyMatch(p -> p.getCheckStatus() == 1);
        boolean dirContainsSemiSelected = permissionList.stream().anyMatch(p -> p.getCheckStatus() == 2);

        if (dirContainsSemiSelected) {
            return 2;
        } else if (dirContainsUnSelected) {
            if (dirContainsSelected) {
                return 2;
            } else {
                return 0;
            }

        } else {
            //!dirContainsUnSelected
            return 1;
        }

    }


    private EHPermission buildFullPermissionTree(List<EHPermission> permissionList,boolean withEmptyDir) {

        EHPermission rootDirectory = permissionList.stream().filter(p -> p.getId().equals("0")).findFirst().get();

        buildSubPermissionTree(rootDirectory, permissionList, withEmptyDir);

        return rootDirectory;
    }


    private void buildSubPermissionTree(EHPermission currentPerm, List<EHPermission> allPerms, boolean withEmptyDir) {

        val childPerms = allPerms.stream().filter(p -> p.getParentId().equals(currentPerm.getId())).collect(Collectors.toList());

        if (currentPerm.getType().equals(PermissionType.Directory) && !CollectionUtils.isEmpty(childPerms)) {

            childPerms.stream().
                    filter(p -> p.getType().equals(PermissionType.Directory))
                    .forEach(p -> buildSubPermissionTree(p, allPerms,withEmptyDir));

            if(!withEmptyDir) {
                //remove empty sub dir
                childPerms.removeIf(childPerm -> childPerm.getType().equals(PermissionType.Directory) && CollectionUtils.isEmpty(childPerm.getChildren()));
            }
            currentPerm.setChildren(childPerms);


        }

    }

    /**
     * All permissions which loaded from this method are ONLY the type P as all related associated directories can be inferred from parentId of table eh_permission.
     *
     * @param orgId
     * @return
     */
    public List<EHPermission> findByOrgId(String orgId) {

        List<EHOrgPermission> orgPermissionList = orgPermissionMapper.selectList(Wrappers.lambdaQuery(EHOrgPermission.class)
                .eq(EHOrgPermission::getOrgId, orgId));

        Set<String> permissionSet = orgPermissionList.stream().map(EHOrgPermission::getPermissionId).collect(Collectors.toSet());

        if (permissionSet.size() > 0) {
            return permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class).in(EHPermission::getId, permissionSet));
        } else {
            return Collections.EMPTY_LIST;
        }
    }


    public List<EHPermission> findByRoleId(String roleId) {
        val rolePermissionLambdaQueryWrapper = Wrappers.lambdaQuery(EHRolePermission.class).eq(EHRolePermission::getRoleId, roleId);

        List<EHRolePermission> rolePermissionList = rolePermissionMapper.selectList(rolePermissionLambdaQueryWrapper);

        Set<String> permissionIds = rolePermissionList.stream().map(EHRolePermission::getPermissionId).collect(Collectors.toSet());

        if (permissionIds.size() > 0) {
            return permissionMapper.selectList(Wrappers.lambdaQuery(EHPermission.class).in(EHPermission::getId, permissionIds));
        } else {
            return Collections.EMPTY_LIST;
        }

    }

    public EHPermission updateOrgPermissions(String orgId, List<String> updatedPermissionIds) {

        EHOrganization org = organizationMapper.selectById(orgId);

        if (org == null) throw new EHApplicationException("org id " + orgId + " is not exist.");


        List<String> existPermissionIds = orgPermissionMapper.selectList(Wrappers.lambdaQuery(EHOrgPermission.class)
                .eq(EHOrgPermission::getOrgId, orgId)).stream().map(op -> op.getPermissionId()).collect(Collectors.toList());

        List<String> toBeDeletedPermissionIds = existPermissionIds.stream().filter(ep -> !updatedPermissionIds.contains(ep)).collect(Collectors.toList());

        List<String> toBeInsertedPermissionIds = updatedPermissionIds.stream().filter(np -> !existPermissionIds.contains(np)).collect(Collectors.toList());

        List<EHRolePermission> existRolePermissionList = rolePermissionMapper.findByOrgId(orgId);

        //check if permission still used in some roles in current org
        List<EHRolePermission> toBeDeletedRolePermissionsInUse = existRolePermissionList.stream().filter(p -> toBeDeletedPermissionIds.contains(p.getPermissionId())).collect(Collectors.toList());

        if (toBeDeletedRolePermissionsInUse.size() > 0) {

            Map<String, List<EHRolePermission>> PermId2RolePermsMap = toBeDeletedRolePermissionsInUse.stream().collect(
                    Collectors.groupingBy(EHRolePermission::getPermissionId, HashMap::new, Collectors.toCollection(ArrayList::new))
            );

            String permId = PermId2RolePermsMap.keySet().iterator().next(); //Only show one permId error information one time.
            EHPermission permission = permissionMapper.selectById(permId);
            List<String> roleIds = PermId2RolePermsMap.get(permId).stream().map(rp->rp.getRoleId()).collect(Collectors.toList());
            List<String> roleNamesInUse = roleMapper.selectBatchIds(roleIds).stream().map(r->r.getRoleName()).collect(Collectors.toList());
            throw new EHApplicationException("permission " + permission.getAuthority() + " is used by roles " + roleNamesInUse.stream().collect(Collectors.joining(",")) + ", organization permission update failed.");
        }

        orgPermissionMapper.delete(Wrappers.lambdaQuery(EHOrgPermission.class)
                .eq(EHOrgPermission::getOrgId, orgId)
                .in(EHOrgPermission::getPermissionId, toBeDeletedPermissionIds));


        toBeInsertedPermissionIds.stream().forEach(permissionId ->
        {
            EHOrgPermission orgPermission = EHOrgPermission.builder().permissionId(permissionId).orgId(orgId).build();
            orgPermissionMapper.insert(orgPermission);
        });

        return rebuildOrgPermissionTree(orgId);
    }

    @ReloadRoleHierarchy
    public EHRole updateRolePermissions(String roleId, List<String> updatedPermissionIds) {

        EHRole role = roleMapper.selectOne(Wrappers.lambdaQuery(EHRole.class).eq(EHRole::getId, roleId));

        if (role == null) throw new EHApplicationException("role id " + roleId + " is not exist.");

        rolePermissionMapper.delete(Wrappers.lambdaQuery(EHRolePermission.class).eq(EHRolePermission::getRoleId,roleId));

        if (updatedPermissionIds != null && updatedPermissionIds.size() > 0) {

            updatedPermissionIds.forEach(permId -> {
                //check if org has the permission before insert.
                EHOrgPermission orgPermission = orgPermissionMapper.selectOne(Wrappers.lambdaQuery(EHOrgPermission.class)
                        .eq(EHOrgPermission::getPermissionId, permId)
                        .eq(EHOrgPermission::getOrgId, role.getOrgId()));

                Optional.ofNullable(orgPermission).ifPresentOrElse(
                        (p) -> rolePermissionMapper.insert(EHRolePermission.builder().roleId(roleId).permissionId(permId).build())
                        ,
                        () -> {
                            EHOrganization org = organizationMapper.selectById(role.getId());
                            throw new EHApplicationException("Permission id " + permId + " is not assigned to organization " + org.getName() + ".");
                        }
                );
            });

        }

        val permissionList = findByRoleId(roleId);

        role.setPermissions(permissionList);

        return role;


    }


    public Page<Map<String, Object>> getPageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().selectMapsPage(page, qw);

    }

    public Page<Map<String, Object>> getRolePermissionPageData(Page<Map<String, Object>> page, QueryWrapper qw) {

        return getBaseMapper().queryRolePermissionPageData(page, qw);

    }


}
