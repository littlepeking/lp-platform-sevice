package com.enhantec.security.jwt;

import com.enhantec.security.web.authentication.JwtAuthFailureHandler;
import com.enhantec.security.web.authentication.JwtAuthSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class  JWTConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Autowired
    private JWTTokenProvider tokenProvider;

    @Autowired
    private JwtAuthFailureHandler webAuthFailureHandler;


    @Autowired
    private JwtAuthSuccessHandler webAuthSuccessHandler;

    @Override
    public void configure(HttpSecurity http) {
        JWTFilter customFilter = new JWTFilter(tokenProvider, webAuthSuccessHandler, webAuthFailureHandler);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
