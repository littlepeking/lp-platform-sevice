package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.service.impl.EHBaseServiceImpl;
import com.enhantec.security.common.enums.PermissionType;
import com.enhantec.security.common.model.EHOrganization;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.service.EHOrganizationService;
import com.enhantec.security.common.mapper.EHOrganizationMapper;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author johnw
 * @description 针对表【eh_organization】的数据库操作Service实现
 * @createDate 2022-06-14 23:40:27
 */
@Service
public class EHOrganizationServiceImpl extends EHBaseServiceImpl<EHOrganizationMapper, EHOrganization>
        implements EHOrganizationService {

    public EHOrganization createOrUpdate(EHOrganization organization){
        if(!StringUtils.hasLength(organization.getId())){
           val count = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class)
            .eq(EHOrganization::getCode,organization.getCode()));
           if(count>0) throw new EHApplicationException("Code "+organization.getCode()+" is already exist.");
        }

        return saveOrUpdateAndRetE(organization);
    }

    public void deleteById(String orgId) {

        EHOrganization org = baseMapper.selectById(orgId);

        if (org == null) throw new EHApplicationException("org id " + orgId + " is not exist.");

        if (orgId.equals("0"))
            throw new EHApplicationException("Organization " + org.getName() + " is root organization, delete is not allowed.");

        long subOrgCount = baseMapper.selectCount(Wrappers.lambdaQuery(EHOrganization.class).eq(EHOrganization::getParentId, orgId));

        if (subOrgCount > 0)
            throw new EHApplicationException("Organization " + org.getName() + " still has child organizations, delete is not allowed.");

        baseMapper.deleteById(orgId);

    }

    public EHOrganization buildOrgTree() {

        List<EHOrganization> organizationList = list();

        EHOrganization rootOrg = organizationList.stream().filter(p -> p.getId().equals("0")).findFirst().get();

        buildSubOrgTree(rootOrg, organizationList);

        return rootOrg;
    }


    private void buildSubOrgTree(EHOrganization currentOrg, List<EHOrganization> allOrganizations) {

        val childOrgs = allOrganizations.stream().filter(p -> p.getParentId().equals(currentOrg.getId())).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(childOrgs)) {

            currentOrg.setChildren(childOrgs);

            childOrgs.stream().forEach(p -> buildSubOrgTree(p, allOrganizations));

        }

    }

}




