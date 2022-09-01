package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.service.impl.EHBaseServiceImpl;
import com.enhantec.security.common.mapper.EHUserMapper;
import com.enhantec.security.common.mapper.EHUserRoleMapper;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.EHUserRole;
import com.enhantec.security.common.service.EHRoleService;
import com.enhantec.security.common.service.EHUserService;
import com.enhantec.security.core.enums.AuthType;
import com.enhantec.security.core.jwt.JwtAuthException;
import com.enhantec.security.core.ldap.LDAPUser;
import com.enhantec.security.core.ldap.LdapUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【EH_USER】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
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
            user.setCredentialsExpired(false);


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

        return saveOrUpdateAndRetE(user);

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

        if(user == null) throw new JwtAuthException("s-usr-usernameNotExist");

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




