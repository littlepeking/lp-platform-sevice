package com.enhantec.security.common.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.security.common.models.EHRolePermission;
import com.enhantec.security.common.models.EHUserRole;
import org.springframework.stereotype.Repository;

@Repository
public interface EHRolePermissionMapper extends BaseMapper<EHRolePermission> {
}
