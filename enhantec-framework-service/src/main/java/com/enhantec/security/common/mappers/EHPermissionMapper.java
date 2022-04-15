package com.enhantec.security.common.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.security.common.models.EHPermission;
import com.enhantec.security.common.models.EHRole;
import org.springframework.stereotype.Repository;

@Repository
public interface EHPermissionMapper extends BaseMapper<EHPermission> {
}
