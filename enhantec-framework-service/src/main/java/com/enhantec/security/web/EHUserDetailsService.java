package com.enhantec.security.web;

import com.enhantec.common.services.LdapService;
import com.enhantec.security.base.EHAuthority;
import com.enhantec.security.base.EHUser;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        logger.debug("Authenticating {}", userName);

        String lowercaseLogin = userName.toLowerCase(Locale.ENGLISH);


        EHUser user =  getJdbcTemplate().queryForObject("select username,password,enabled,login_name from users where username = ?", new Object[]{userName}, (rs, rowNum) ->
                new EHUser(
                        rs.getString("username"),
                        rs.getString("login_name"),
                        rs.getString("password"),
                        rs.getBoolean("enabled"),
                        true,
                        true,
                        true,
                        null
                ));

            List<EHAuthority> authorities = getJdbcTemplate().query(
                    "select username,authority from authorities where username = ?",new Object[]{userName},(rs,rowNum)-> new EHAuthority(
                            rs.getString("authority")
                    )
            );

            user.setAuthorities(authorities);

            return user;

    }


    private Collection<? extends GrantedAuthority> getAuthorities(String subject) {

        //  if(subject.equals("cn=john"))
        return Arrays.stream(new String[]{"admin"}).map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}