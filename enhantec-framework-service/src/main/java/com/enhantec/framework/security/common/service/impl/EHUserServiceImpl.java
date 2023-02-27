/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.framework.security.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.security.common.mapper.EHUserMapper;
import com.enhantec.framework.security.common.mapper.EHUserRoleMapper;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.model.EHUserRole;
import com.enhantec.framework.security.common.service.EHRoleService;
import com.enhantec.framework.security.common.service.EHUserService;
import com.enhantec.framework.security.core.enums.AuthType;
import com.enhantec.framework.security.core.auth.EHAuthException;
import com.enhantec.framework.security.core.ldap.LDAPUser;
import com.enhantec.framework.security.core.ldap.LdapUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【EH_USER】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
@DS(DSConstants.DS_MASTER)
public class EHUserServiceImpl extends EHBaseServiceImpl<EHUserMapper, EHUser>
    implements EHUserService {


    private final LdapUserRepository ldapUserRepository;
    private final LdapTemplate ldapTemplate;
    private final PasswordEncoder passwordEncoder;

    private final EHRoleService roleService;

    private final EHUserRoleMapper userRoleMapper;


    public EHUser createOrUpdate(EHUser user) {

        if(!StringUtils.hasLength(user.getId())) {

            //Register user
            validUsername(user);

            if (AuthType.LDAP.equals(user.getAuthType())) {

//                boolean success = ldapTemplate.authenticate("", "(sAMAccountName=" + user.getUsername() + ")",
//                        user.getPassword());
//                if (!success) {
//                    throw new EHApplicationException("s-usr-usernamePasswordNotMatch");
//                }

                LDAPUser ldapUser = ldapUserRepository.findBysAMAccountName(user.getUsername()).get();

                if(ldapUser!=null) {
                    user.setDomainUsername(ldapUser.getFullName().toString());
                }else {
                    throw new EHApplicationException("s-usr-ADUserNotFound",user.getUsername());
                }

            }else {
                //only basic auth type record password hash.
                  user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            //Set default access properties to new user.
            user.setAccountLocked(false);
            user.setEnabled(true);
            user.setPasswordChangedTime(LocalDateTime.now());


        }else {

            //Update user

            EHUser originUserInfo = baseMapper.selectById(user.getId());

            user.setAuthType(originUserInfo.getAuthType()); //authType cannot be updated

            if(!originUserInfo.getUsername().equals(user.getUsername())) {
                //update to a new username (All tables must reference userId instead of username as username can be updated!!!)
                validUsername(user);

            }

            if(user.getAuthType().equals(AuthType.BASIC)){
                //Check if user want change to new password.
                if(StringUtils.hasLength(user.getPassword())){
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    user.setPasswordChangedTime(LocalDateTime.now());
                        //no need original password at all here..
//                    if(!StringUtils.hasLength(user.getOriginalPassword()))
//                        throw new EHApplicationException("s-usr-originPasswordNotProvided");
//
//                    if( passwordEncoder.matches(user.getOriginalPassword(),originUserInfo.getPassword())){
//
//                        user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//                    }else {
//                        throw new EHApplicationException("s-usr-passwordNotMatch");
//                    }
                }
                else{
                    user.setPassword(null);
                }

            }

            //enable attribute can only be changed from enable method.
            user.setEnabled(originUserInfo.isEnabled());

        }

        return saveOrUpdateRetE(user);

    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {

        if(!StringUtils.hasText(oldPassword)) throw new EHApplicationException("s-usr-originPasswordNotProvided");
        if(!StringUtils.hasText(newPassword)) throw new EHApplicationException("s-usr-newPasswordNotProvided");

        EHUser user = getOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,username));

        if(user==null) throw new EHApplicationException("s-usr-usernameNotExist");

        if(!passwordEncoder.matches(oldPassword,user.getPassword()))
            throw new EHApplicationException("s-usr-passwordNotMatch");

        val updateWrapper = Wrappers.lambdaUpdate(EHUser.class)
                .set(EHUser::getPassword,passwordEncoder.encode(newPassword))
                .set(EHUser::getPasswordChangedTime,LocalDateTime.now())
                .set(EHUser::getEditWho,username)
                .eq(EHUser::getId,user.getId());
        baseMapper.updateTr(null, updateWrapper);

    }

    public void enable(String userId){

        checkIfUserIdExists(userId);

        val updateWrapper = Wrappers.lambdaUpdate(EHUser.class).set(EHUser::isEnabled,true)
                .eq(EHUser::getId,userId);
        baseMapper.update(null, updateWrapper);
    }

    public void disable(String userId){

        checkIfUserIdExists(userId);

        val updateWrapper = Wrappers.lambdaUpdate(EHUser.class).set(EHUser::isEnabled,false)
                .eq(EHUser::getId,userId);

        baseMapper.update(null, updateWrapper);
    }

    @Deprecated
    //only used in system data init phase.
    public void delete(String userId){

        LambdaQueryWrapper<EHUserRole> wrapper = Wrappers.lambdaQuery(EHUserRole.class).eq(EHUserRole::getUserId,userId);

        userRoleMapper.delete(wrapper);

        baseMapper.deleteById(userId);
    }

    private void validUsername(EHUser user) {
        if (!user.getUsername().equals(user.getUsername().toLowerCase()))
            throw new EHApplicationException("s-usr-usernameMustBeLowerCase");

        long count = count(Wrappers.lambdaQuery(EHUser.class)
                .eq(EHUser::getUsername, user.getUsername())
                .eq(EHUser::isEnabled,true)
        );

        if (count > 0) throw new EHApplicationException("s-usr-usernameInUse");
    }

    public void checkIfUsernameExists(String username){

        EHUser user = getBaseMapper().selectOne(Wrappers.lambdaQuery(EHUser.class).eq(EHUser::getUsername,username));

        if(user == null) throw new EHAuthException("s-usr-usernameNotExist");

    }

    public void checkIfUserIdExists(String userId){

        EHUser user = getBaseMapper().selectById(userId);

        if(user == null) throw new EHApplicationException("s-usr-usernameNotFound",userId);

    }


    public List<EHUser> findAll() {
        List<EHUser> userList = getBaseMapper().selectList(Wrappers.lambdaQuery(EHUser.class));
        return userList;

    }


    public Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        return getBaseMapper().selectMapsPage(page, qw);
    }

    

}




