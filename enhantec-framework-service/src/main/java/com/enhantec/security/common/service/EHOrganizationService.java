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



package com.enhantec.security.common.service;

import com.enhantec.common.service.EHBaseService;
import com.enhantec.security.common.model.EHOrganization;

import java.util.List;

/**
* @author johnw
* @description 针对表【eh_organization】的数据库操作Service
* @createDate 2022-06-14 23:40:27
*/
public interface EHOrganizationService extends EHBaseService<EHOrganization> {

    void deleteById(String orgId);

    List<EHOrganization> buildOrgTree();

    List<EHOrganization> buildOrgTreeByUserId(String userId);

    List<EHOrganization> buildOrgTreeByPermId(String permissionId);

    EHOrganization createOrUpdate(EHOrganization organization);

}
