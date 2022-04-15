package com.enhantec.security.core.ldap;

import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.services.EHUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@RequiredArgsConstructor
@Component
public class LDAPAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private ApplicationContext appContext;

    private final LdapUserRepository ldapUserRepository;

    private final LdapTemplate ldapTemplate;

    private final EHUserDetailsService ehUserDetailsService;

    private final LdapService ldapService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        String lowercaseLogin = username.toLowerCase(Locale.ENGLISH);

//        ldapUserRepository.findAll().forEach(System.out::println);
//        List<String> res = ldapTemplate.list("");
//        res.stream().forEach(System.out::println);

        boolean success = ldapTemplate.authenticate("", "(sAMAccountName="+lowercaseLogin+")", authentication.getCredentials().toString());

        if(!success){
            throw new BadCredentialsException("uid and password does not match:\n ");
        }

        Optional<LDAPUser> ldapUser = ldapUserRepository.findBysAMAccountName(lowercaseLogin);


//        System.out.println(success);
//        ldapUserRepository.findAll().forEach(System.out::println);
//        List<String> res = ldapTemplate.list("");
//        System.out.println(res.size());
//        LdapQuery query = LdapQueryBuilder.query()
//                .where("sAMAccountName").is("john");
//        LDAPUser person = ldapTemplate.findOne(query, LDAPUser.class);
//        System.out.println(person);
//        Set<String> groups = ldapService.getAllGroupsForUserRecursively(username);


        EHUser user = ehUserDetailsService.getUserInfo(lowercaseLogin);
        if(user==null){
           user =  ehUserDetailsService.createDomainUser(ldapUser.get().getUid(),ldapUser.get().getId().toString());
        }

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
