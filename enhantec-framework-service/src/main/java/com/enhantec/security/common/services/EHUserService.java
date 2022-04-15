package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.security.common.mappers.EHUserMapper;
import com.enhantec.security.common.models.EHUser;
import org.springframework.stereotype.Service;

@Service
public class EHUserService extends ServiceImpl<EHUserMapper, EHUser> {
}
