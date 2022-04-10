package com.enhantec.security.web;

import com.enhantec.security.common.models.EHAuthority;
import com.enhantec.security.common.models.EHUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component
public class EHUserDetailsService extends JdbcDaoSupport implements UserDetailsService {

    @Autowired
    public void setJdbcTemplate(DataSource dataSource) {
        setDataSource(dataSource);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    final PasswordEncoder passwordEncoder;

    public EHUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EHUser loadUserByUsername(final String userName) {

        EHUser user = getUserInfo(userName);

        if(user==null) throw new UsernameNotFoundException("username is not exists");

        return user;

    }

    public EHUser getUserInfo(final String userName) {

        logger.debug("Authenticating {}", userName);

        String lowercaseLogin = userName.toLowerCase(Locale.ENGLISH);


        List<EHUser> users =  getJdbcTemplate().query("select username,password,enabled,domain_username from users where username = ?", new Object[]{lowercaseLogin}, (rs, rowNum) ->
                new EHUser(
                        rs.getString("username"),
                        rs.getString("domain_username"),
                        rs.getString("password"),
                        rs.getBoolean("enabled"),
                        true,
                        true,
                        true,
                        null
                )
        );

        EHUser user = users.isEmpty() ? null: users.get(0);

        if(user==null) return null;

        else {

            List<EHAuthority> authorities = getJdbcTemplate().query(
                    "select username,authority from authorities where username = ?", new Object[]{userName}, (rs, rowNum) -> new EHAuthority(
                            rs.getString("authority")
                    )
            );

            user.setAuthorities(authorities);

            return user;
        }

    }

     public EHUser createDomainUser(final String userName, final String domainUserName){

        return new EHUser(userName,domainUserName,"",true,true,true,true,new ArrayList<>());
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String subject) {

        //  if(subject.equals("cn=john"))
        return Arrays.stream(new String[]{"admin"}).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
