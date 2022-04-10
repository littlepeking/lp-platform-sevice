package com.enhantec.security.ldap;

import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.web.EHUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class LDAPAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final LdapUserRepository ldapUserRepository;

    private final EHUserDetailsService ehUserDetailsService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return;
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        String lowercaseLogin = username.toLowerCase(Locale.ENGLISH);

        LDAPUser ldapUser =  ldapUserRepository.findByUsernameAndPassword(lowercaseLogin, authentication.getCredentials().toString())
                .orElseThrow(()-> new BadCredentialsException("username and password does not match."));

        EHUser user = ehUserDetailsService.getUserInfo(lowercaseLogin);
        if(user==null){
           user =  ehUserDetailsService.createDomainUser(ldapUser.getUsername(),ldapUser.getId().toString());
        }

        return user;
    }
}
