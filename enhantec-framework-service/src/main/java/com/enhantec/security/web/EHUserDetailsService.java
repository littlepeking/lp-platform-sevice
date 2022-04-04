package com.enhantec.security.web;

import com.enhantec.common.services.LdapService;
import com.enhantec.security.jwt.JWTUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class EHUserDetailsService implements UserDetailsService {

    @Autowired
    public LdapService ldapService;


    private Logger logger = LoggerFactory.getLogger(getClass());

    final PasswordEncoder passwordEncoder;

    public EHUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(final String userName) {
        logger.debug("Authenticating {}", userName);

        String lowercaseLogin = userName.toLowerCase(Locale.ENGLISH);

        boolean isDomainUser = true;

        JWTUser user = null;
        if(isDomainUser) {
            user = ldapService.authenticate(lowercaseLogin);
        }else{
            //username/password load from db

            logger.info("login user:"+ user);
            //password type and format see: https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-encoding
            //return new User(userName,passwordEncoder.encode("123"), true,true,true,true, AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));

            user = null;

            return user;

        }


        if(user==null) {
            throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database/AD");
        }else {
            getAuthorities(user.getUsername());
        }

        return user;

    }


    private Collection<? extends GrantedAuthority> getAuthorities(String subject) {

        //  if(subject.equals("cn=john"))
        return Arrays.stream(new String[]{"admin"}).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
