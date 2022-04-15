package com.enhantec.security.common.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.models.EHUserRole;
import org.springframework.stereotype.Repository;

@Repository
public interface EHUserRoleMapper extends BaseMapper<EHUserRole> {
}
