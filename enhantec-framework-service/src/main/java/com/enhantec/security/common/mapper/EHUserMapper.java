package com.enhantec.security.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.security.common.model.EHUser;
import org.springframework.stereotype.Repository;

@Repository
public interface EHUserMapper extends EHBaseMapper<EHUser> {

}
