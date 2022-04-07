package com.enhantec.security.web;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.jwt.JWTConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
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

    JWTConfigurer jwtConfigurer;

    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProblemSupport securityProblemSupport;

    private final DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("12345678")).roles("USER", "ADMIN");
//        auth.inMemoryAuthentication().withUser("john").password(passwordEncoder.encode("12345678")).roles("USER");

        auth.jdbcAuthentication()
             //   .withDefaultSchema()
                .dataSource(dataSource);
//                .withUser("john")
//                .password(passwordEncoder.encode("12345678"))
//                .roles("USER");



    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
//        validateCodeFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
//        validateCodeFilter.setApplicationProperties(applicationProperties);
//        validateCodeFilter.afterPropertiesSet();

        http
                //表单登录
                .requestMatchers().antMatchers("/login","/web/**")
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
