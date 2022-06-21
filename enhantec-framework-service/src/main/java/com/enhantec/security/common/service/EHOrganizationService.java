package com.enhantec.security.common.service;

import com.enhantec.common.service.EHBaseService;
import com.enhantec.security.common.model.EHOrganization;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author johnw
* @description 针对表【eh_organization】的数据库操作Service
* @createDate 2022-06-14 23:40:27
*/
public interface EHOrganizationService extends EHBaseService<EHOrganization> {

    void deleteById(String orgId);

    EHOrganization buildOrgTree();

    EHOrganization createOrUpdate(EHOrganization organization);

}
