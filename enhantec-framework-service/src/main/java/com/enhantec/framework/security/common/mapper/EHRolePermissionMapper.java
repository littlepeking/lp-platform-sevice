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



package com.enhantec.framework.security.common.mapper;

import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.framework.security.common.model.EHRolePermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author johnw
* @description 针对表【eh_role_permission】的数据库操作Mapper
* @createDate 2022-06-15 15:56:58
* @Entity com.enhantec.security.common.model.EHRolePermission
*/
public interface EHRolePermissionMapper extends EHBaseMapper<EHRolePermission> {

    List<EHRolePermission> findByOrgId(@Param("orgId") String orgId);

    List<EHRolePermission> findByPermIdAndOrgId(@Param("permId") String permId,@Param("orgId") String orgId);
}




