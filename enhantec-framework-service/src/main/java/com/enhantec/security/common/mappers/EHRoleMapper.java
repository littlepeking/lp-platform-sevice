package com.enhantec.security.common.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.models.EHRole;
import com.enhantec.security.common.models.EHUserRole;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface EHRoleMapper extends BaseMapper<EHRole> {


}
