package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.core.enums.AuthType;
import com.enhantec.security.core.ldap.LDAPUser;
import com.enhantec.security.core.ldap.LdapUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Transactional
public interface EHUserService extends IService<EHUser> {

    public List<EHUser> findAll();

    public EHUser createUser(String username, String password, AuthType authType);

    public void checkIfUserExists(String username);

    public Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);


}
