package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.TestReceiptMapper;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.EHUserService;
import com.enhantec.security.common.service.TestReceiptService;
import com.enhantec.security.core.enums.AuthType;
import com.enhantec.security.core.ldap.LDAPUser;
import com.enhantec.security.core.ldap.LdapUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【EH_USER】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
public class EHUserServiceImpl extends ServiceImpl<EHUserMapper, EHUser>
    implements EHUserService {


    private final LdapUserRepository ldapUserRepository;
    private final LdapTemplate ldapTemplate;
    private final PasswordEncoder passwordEncoder;


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
                .password(passwordEncoder.encode(password))
                .authType(authType)
                .enabled(true)
                .build();

        save(user);

        return user;
    }

    public void checkIfUserExists(String username){

        EHUser user = getBaseMapper().selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,username));

        if(user == null) throw new UsernameNotFoundException("username "+username+" is not exists.");

    }


    public List<EHUser> findAll() {
        List<EHUser> userList = getBaseMapper().selectList(Wrappers.lambdaQuery(EHUser.class));
        return userList;

    }


    public Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        return getBaseMapper().selectMapsPage(page, qw);
    }


}




