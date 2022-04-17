package com.enhantec.security.common.services;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mappers.EHUserMapper;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.core.enums.AuthType;
import com.enhantec.security.core.ldap.LDAPUser;
import com.enhantec.security.core.ldap.LdapUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class EHUserService extends ServiceImpl<EHUserMapper, EHUser> {

    private final LdapUserRepository ldapUserRepository;
    private final LdapTemplate ldapTemplate;

    public EHUser createUser(String username, String password, AuthType authType) {

        if(!username.equals(username.toLowerCase()))
            throw new EHApplicationException("username must be lowercase");

        EHUser user = getOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername, username));

        if (user != null) {
            throw new EHApplicationException("user name is already exist");
        }

        String domainUserName ="";

        if(AuthType.LDAP.equals(authType)){

            boolean success = ldapTemplate.authenticate("", "(sAMAccountName="+ username+")",
                    password);

            if(!success){
                throw new EHApplicationException("LDAP auth failed: username and password does not match.");
            }

            LDAPUser ldapUser = ldapUserRepository.findBysAMAccountName(username).get();

            domainUserName = ldapUser.getFullName().toString();

        }

        user =EHUser.builder()
                .username(username)
                .domainUsername(domainUserName)
                .password(password)
                .authType(authType)
                .enabled(true)
                .build();


        return user;
    }

    public void checkIfUserExists(String username){

        boolean userExists = getBaseMapper().exists(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,username));

        if(!userExists) throw new UsernameNotFoundException("username "+username+" is not exists.");

    }
}
