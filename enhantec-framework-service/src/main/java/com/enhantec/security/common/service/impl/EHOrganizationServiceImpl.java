package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.service.impl.EHBaseServiceImpl;
import com.enhantec.security.common.mapper.EHOrgPermissionMapper;
import com.enhantec.security.common.mapper.EHOrganizationMapper;
import com.enhantec.security.common.model.EHOrgPermission;
import com.enhantec.security.common.model.EHOrganization;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.service.EHOrganizationService;
import com.enhantec.security.common.service.EHPermissionService;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author johnw
 * @description 针对表【eh_organization】的数据库操作Service实现
 * @createDate 2022-06-14 23:40:27
 */
@Service
@RequiredArgsConstructor
public class EHOrganizationServiceImpl extends EHBaseServiceImpl<EHOrganizationMapper, EHOrganization>
        implements EHOrganizationService {
    private final EHOrgPermissionMapper orgPermissionMapper;

    private final EHPermissionService permissionService;

    private final EHRoleService roleService;

    public EHOrganization createOrUpdate(EHOrganization organization){
        if(!StringUtils.hasLength(organization.getId())){
            validOrg(organization);
        }else {
            val existOrg = baseMapper.selectOne(Wrappers.lambdaQuery(EHOrganization.class).eq(EHOrganization::getId,organization.getId()));
            if(existOrg==null) throw new EHApplicationException("s-org-idNotExist",organization.getId());
            if(!organization.getCode().equals(existOrg.getCode())){
                validOrg(organization);
            }
        }

        return saveOrUpdateAndRetE(organization);
    }

    private void validOrg(EHOrganization organization){
        val count = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class)
                .eq(EHOrganization::getCode,organization.getCode()));
        if(count>0) throw new EHApplicationException("s-org-codeExist",organization.getCode());
    }

    @Deprecated
    //org should can only be delete in dev env. In production, org can only be disable as related biz data will be inconsistent after the org record delete.
    public void deleteById(String orgId) {

        EHOrganization org = baseMapper.selectById(orgId);

        if (org == null) throw new EHApplicationException("s-org-idNotExist",orgId);

        if (orgId.trim().equals("0"))
            throw new EHApplicationException("s-org-rootOrgDeleteNotAllow", org.getName());

        long subOrgCount = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class).eq(EHOrganization::getParentId, orgId));

        if (subOrgCount > 0)
            throw new EHApplicationException("s-org-childOrgExist",org.getName());
        //remove related org permissions
        permissionService.updateOrgPermissions(orgId, Collections.EMPTY_LIST);
        //remove related org roles
        List<EHRole> roles = roleService.findByOrgId(orgId);
        roles.forEach(r->  roleService.delete(r.getId()));
        //remove org
        baseMapper.deleteById(orgId);

    }


    public List<EHOrganization> buildOrgTree() {

        List<EHOrganization> organizationList = list();
        return  buildOrgTree(organizationList);
    }


    public List<EHOrganization> buildOrgTree(List<EHOrganization> organizationList) {

        EHOrganization rootOrg = organizationList.stream().filter(p -> p.getId().equals("0")).collect(Collectors.toList()).stream().findFirst().get();

         buildSubOrgTree(rootOrg, organizationList);

        return Arrays.asList(new EHOrganization[]{rootOrg});
    }


    private void buildSubOrgTree(EHOrganization currentOrg, List<EHOrganization> allOrganizations) {

        val childOrgs = allOrganizations.stream().filter(p -> p.getParentId()!=null && p.getParentId().equals(currentOrg.getId())).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(childOrgs)) {

            currentOrg.setChildren(childOrgs);

            childOrgs.stream().forEach(p -> buildSubOrgTree(p, allOrganizations));

        }

    }

    public List<EHOrganization> buildPermissionOrgTree(String permissionId) {

        List<EHOrgPermission> orgPermissionList = orgPermissionMapper.selectList(Wrappers.lambdaQuery(EHOrgPermission.class)
                .eq(EHOrgPermission::getPermissionId, permissionId));

        List<EHOrganization> organizationList = list();

        organizationList.stream().forEach(o -> {

            o.setCheckStatus(orgPermissionList.stream().anyMatch(op -> op.getOrgId().equals(o.getId())) ? true : false);

        });

        List<EHOrganization> rootOrgs = buildOrgTree(organizationList);
        //No need check sub orgs automatically as permission is checked by org independently
        //rootOrgs.stream().forEach(org->EHTreeHelper.recursivelyCalculateCheckStatus(org));

        return rootOrgs;

    }



}




