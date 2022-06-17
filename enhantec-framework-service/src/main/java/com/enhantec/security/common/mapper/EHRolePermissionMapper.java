package com.enhantec.security.common.mapper;

import com.enhantec.security.common.model.EHRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author johnw
* @description 针对表【eh_role_permission】的数据库操作Mapper
* @createDate 2022-06-15 15:56:58
* @Entity com.enhantec.security.common.model.EHRolePermission
*/
public interface EHRolePermissionMapper extends BaseMapper<EHRolePermission> {


    List<EHRolePermission> findByOrgId(@Param("orgId") String orgId);
}




