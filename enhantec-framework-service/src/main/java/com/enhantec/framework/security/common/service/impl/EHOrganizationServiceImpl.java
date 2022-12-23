/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.framework.security.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DBConst;
import com.enhantec.framework.security.common.mapper.EHOrgPermissionMapper;
import com.enhantec.framework.security.common.mapper.EHOrganizationMapper;
import com.enhantec.framework.security.common.model.EHOrgPermission;
import com.enhantec.framework.security.common.model.EHOrganization;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.service.EHOrganizationService;
import com.enhantec.framework.security.common.service.EHPermissionService;
import com.enhantec.framework.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
@DS(DBConst.DS_MASTER)
@Transactional(rollbackFor = Exception.class)
public class EHOrganizationServiceImpl extends EHBaseServiceImpl<EHOrganizationMapper, EHOrganization>
        implements EHOrganizationService {
    private final EHOrgPermissionMapper orgPermissionMapper;

    private final EHPermissionService permissionService;

    private final EHRoleService roleService;

    public EHOrganization createOrUpdate(EHOrganization organization) {
        if (!StringUtils.hasLength(organization.getId())) {
            validOrg(organization);
        } else {
            val existOrg = baseMapper.selectOne(Wrappers.lambdaQuery(EHOrganization.class).eq(EHOrganization::getId, organization.getId()));
            if (existOrg == null) throw new EHApplicationException("s-org-idNotExist", organization.getId());
            if (!organization.getCode().equals(existOrg.getCode())) {
                validOrg(organization);
            }
        }

        return saveOrUpdateRetE(organization);
    }

    private void validOrg(EHOrganization organization) {
        val count = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class)
                .eq(EHOrganization::getCode, organization.getCode()));
        if (count > 0) throw new EHApplicationException("s-org-codeExist", organization.getCode());
    }

    @Deprecated
    //org should can only be delete in dev env. In production, org can only be disable as related biz data will be inconsistent after the org record delete.
    public void deleteById(String orgId) {

        EHOrganization org = baseMapper.selectByIdTr(orgId);

        if (org == null) throw new EHApplicationException("s-org-idNotExist", orgId);

        if (orgId.trim().equals("0"))
            throw new EHApplicationException("s-org-rootOrgDeleteNotAllow", org.getName());

        long subOrgCount = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class).eq(EHOrganization::getParentId, orgId));

        if (subOrgCount > 0)
            throw new EHApplicationException("s-org-childOrgExist", org.getName());
        //remove related org permissions
        permissionService.updateOrgPermissions(orgId, Collections.EMPTY_LIST);
        //remove related org roles
        List<EHRole> roles = roleService.findByOrgId(orgId);
        roles.forEach(r -> roleService.delete(r.getId()));
        //remove org
        baseMapper.deleteByIdTr(orgId);
        //baseMapper.deleteByIdTr(getById(orgId));

    }

    public List<EHOrganization> buildOrgTree() {

        List<EHOrganization> organizationList = list();
        return buildOrgTree(organizationList);
    }


    public List<EHOrganization> buildOrgTree(List<EHOrganization> organizationList) {

        EHOrganization rootOrg = organizationList.stream().filter(p -> p.getId().equals("0")).collect(Collectors.toList()).stream().findFirst().get();

        buildSubOrgTree(rootOrg, organizationList);

        return Arrays.asList(new EHOrganization[]{rootOrg});
    }


    private void buildSubOrgTree(EHOrganization currentOrg, List<EHOrganization> allOrganizations) {

        val childOrgs = allOrganizations.stream().filter(p -> p.getParentId() != null && p.getParentId().equals(currentOrg.getId())).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(childOrgs)) {

            currentOrg.setChildren(childOrgs);

            childOrgs.stream().forEach(p -> buildSubOrgTree(p, allOrganizations));

        }

    }

    public List<EHOrganization> buildOrgTreeByPermId(String permissionId) {

        List<EHOrgPermission> orgPermissionList = orgPermissionMapper.selectListTr(Wrappers.lambdaQuery(EHOrgPermission.class)
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


    public List<EHOrganization> buildOrgTreeByUserId(String userId) {

        List<EHRole> roleList = roleService.findByUserId(userId);

        if(roleList.size()>0) {

            List<EHOrganization> organizationList = list();

            organizationList.stream().forEach(o -> {

                o.setCheckStatus(roleList.stream().anyMatch(r -> r.getOrgId().equals(o.getId())) ? true : false);

            });

            return buildMinOrgTreeByCheckStatus(organizationList);
        }
        else return Collections.EMPTY_LIST;

    }


    /**
     * build a minimum org tree only contains selected orgs and all their parent orgs.
     *
     * @return
     */
    private List<EHOrganization> buildMinOrgTreeByCheckStatus(List<EHOrganization> organizationList) {

        EHOrganization rootOrg = organizationList.stream().filter(p -> p.getId().equals("0")).collect(Collectors.toList()).stream().findFirst().get();

        if (keepOrgSubTree(rootOrg,organizationList))
            return Arrays.asList(new EHOrganization[]{rootOrg});
        else return Collections.EMPTY_LIST;


    }


    private boolean keepOrgSubTree(EHOrganization currentOrg, List<EHOrganization> organizationList) {

        currentOrg.setChildren(new ArrayList<>());

        var childOrganizations =  organizationList.stream().filter(o-> currentOrg.getId().equals(o.getParentId())).collect(Collectors.toList());

        if(childOrganizations.size()>0) {
            for (EHOrganization childOrg : childOrganizations) {
                if (keepOrgSubTree(childOrg,organizationList)) {
                    currentOrg.getChildren().add(childOrg);
                }
            }
        }
            return currentOrg.getChildren()!=null  && currentOrg.getChildren().size()>0 || currentOrg.getCheckStatus() == true;
        }
    }

