package com.enhantec.security.core.ldap;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.services.EHUserDetailsService;
import com.enhantec.security.common.services.EHUserService;
import com.enhantec.security.core.enums.AuthType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class LDAPAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private ApplicationContext appContext;

    private final LdapUserRepository ldapUserRepository;

    private final LdapTemplate ldapTemplate;

    private final EHUserDetailsService ehUserDetailsService;

    private final EHUserService userService;

    private final ApplicationProperties applicationProperties;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        String lowercaseLogin = authentication.getName();

        //      if authentication type is usernamePassword, throw org.springframework.security.core.AuthenticationException();
        //       ldapUserRepository.findAll().forEach(System.out::println);
        //        List<String> res = ldapTemplate.list("");
        //        res.stream().forEach(System.out::println);


        userService.checkIfUserExists(lowercaseLogin);

        EHUser user = ehUserDetailsService.getUserInfo(lowercaseLogin);

        if(!user.getAuthType().equals(AuthType.LDAP)){
            throw new BadCredentialsException("User auth type is not LDAP and skipped by LDAP auth provider, auth failed. Current user auth type:  " + user.getAuthType());
        }

        boolean success = ldapTemplate.authenticate("", "(sAMAccountName="+lowercaseLogin+")", authentication.getCredentials().toString());

        if(!success){
            throw new BadCredentialsException("LDAP auth failed: username and password does not match.");
        }

        LDAPUser ldapUser = ldapUserRepository.findBysAMAccountName(lowercaseLogin).get();

//        if(user==null){
//            throw new BadCredentialsException("Current domain user is not registered in application.");
//            // user =  ehUserDetailsService.createDomainUser(ldapUser.getSAMAccountName(),ldapUser.getFullName().toString());
//        }

        if(!StringUtils.equals(ldapUser.getFullName().toString(),user.getDomainUsername()))
        {
            userService.saveOrUpdate(user.withDomainUsername(ldapUser.getFullName().toString()));
            user.setDomainUsername(ldapUser.getFullName().toString());
        }

//        System.out.println(success);
//        ldapUserRepository.findAll().forEach(System.out::println);
//        List<String> res = ldapTemplate.list("");
//        System.out.println(res.size());
//        LdapQuery query = LdapQueryBuilder.query()
//                .where("sAMAccountName").is("john");
//        LDAPUser person = ldapTemplate.findOne(query, LDAPUser.class);
//        System.out.println(person);
//        Set<String> groups = ldapService.getAllGroupsForUserRecursively(username);

        return user;
    }

    @PostConstruct
    private void setReferralForContext() {
        LdapTemplate ldapTemplate = appContext.getBean(LdapTemplate.class);// necessary for LdapContextSource to be created
        //https://myshittycode.com/2017/03/26/ldaptemplate-javax-naming-partialresultexception-unprocessed-continuation-references-remaining-name/
        ldapTemplate.setIgnorePartialResultException(true);
        LdapContextSource ldapContextSource = appContext.getBean(LdapContextSource.class);
        //ldapContextSource.setReferral("follow");
        //ldapContextSource.setReferral("ignore");
        ldapContextSource.setPooled(true);
        ldapContextSource.afterPropertiesSet();
    }


}
