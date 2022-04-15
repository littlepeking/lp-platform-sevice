package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.security.common.mappers.EHUserMapper;
import com.enhantec.security.common.models.EHUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EHUserService extends ServiceImpl<EHUserMapper, EHUser> {

    public EHUser createUser(final String userName){

        EHUser user = EHUser.builder().username(userName).domainUsername("").build();
        this.save(user);

        return user;
    }

    public EHUser createDomainUser(final String userName, final String domainUserName){

        EHUser user = EHUser.builder().username(userName).domainUsername(domainUserName).build();
        this.save(user);

        return user;
    }
}
