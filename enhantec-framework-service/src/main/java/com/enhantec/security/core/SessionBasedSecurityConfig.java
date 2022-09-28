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



package com.enhantec.security.core;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.core.ldap.BasicAuthenticationProvider;
import com.enhantec.security.core.ldap.LDAPAuthenticationProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import javax.sql.DataSource;

import static com.enhantec.security.Constants.*;

@RequiredArgsConstructor
@Slf4j
//@EnableWebSecurity(debug = true)
@EnableWebSecurity()
@Order(98)
public class SessionBasedSecurityConfig extends WebSecurityConfigurerAdapter {

    //private Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationProperties applicationProperties;

    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProblemSupport securityProblemSupport;

    private final DataSource dataSource;

    private final BasicAuthenticationProvider basicAuthenticationProvider;

    private final LDAPAuthenticationProvider ldapAuthenticationProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(basicAuthenticationProvider);
        auth.authenticationProvider(ldapAuthenticationProvider);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
//        validateCodeFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
//        validateCodeFilter.setApplicationProperties(applicationProperties);
//        validateCodeFilter.afterPropertiesSet();

        http
                //表单登录
                .requestMatchers().antMatchers(loginUrl,"/web/**")
                .and()
                .authorizeHttpRequests(req->req.antMatchers(loginUrl, "/code/image")
                        .permitAll().anyRequest().authenticated())
                //.httpBasic(Customizer.withDefaults())
//                .formLogin(
//                        form -> form
//                        .loginPage(loginUrl)
//                        //.loginProcessingUrl(loginProcessingUrl)
//                        .defaultSuccessUrl("/"))
//                        .logout(logout -> logout.logoutUrl("/doLogout"))
                .formLogin().and()
                .rememberMe(rm->rm.tokenValiditySeconds(3600))
                .csrf(csrf -> csrf.disable());
    }

}
