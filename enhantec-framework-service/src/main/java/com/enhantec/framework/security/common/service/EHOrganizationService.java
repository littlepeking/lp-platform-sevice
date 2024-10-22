/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/



package com.enhantec.framework.security.common.service;

import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.security.common.model.EHOrganization;

import java.util.List;

/**
* @author johnw
* @description 针对表【eh_organization】的数据库操作Service
* @createDate 2022-06-14 23:40:27
*/
public interface EHOrganizationService extends EHBaseService<EHOrganization> {

    void deleteById(String orgId);

    List<EHOrganization> buildOrgTree();

    EHOrganization buildSubOrgTreeByOrgId(String orgId);

    List<EHOrganization> buildOrgTreeByUserId(String userId);

    List<EHOrganization> buildOrgTreeByPermId(String permissionId);

    EHOrganization createOrUpdate(EHOrganization organization);

}
