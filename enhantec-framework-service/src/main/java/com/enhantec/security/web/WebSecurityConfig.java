package com.enhantec.security.web;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.core.validate.code.ValidateCodeFilter;
import com.enhantec.security.jwt.JWTConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javafx.collections.MappingChange;
import io.jsonwebtoken.lang.Maps;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.HashMap;

import static com.enhantec.security.Constants.loginUrl;
import static com.enhantec.security.Constants.swaggerUrl;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    DataSource dataSource;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    JWTConfigurer jwtConfigurer;

    @Autowired ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().mvcMatchers("/static/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
       auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("12345678")).roles("USER","ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
//        validateCodeFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
//        validateCodeFilter.setApplicationProperties(applicationProperties);
//        validateCodeFilter.afterPropertiesSet();

        http
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.loginProcessingUrl("/login")
                        .successHandler(jsonAuthSuccessHandler())
                        .failureHandler(jsonAuthFailureHandler()))    //   .userDetailsService(userDetailsService)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .antMatchers(loginUrl, swaggerUrl, "/swagger-resources/**", "/v3/api-docs", "/code/image")
                .permitAll().anyRequest().authenticated().and()
                .exceptionHandling(exHandler -> exHandler.accessDeniedHandler(CustomAccessDeniedHandler()))
                //.authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .csrf(csrf -> csrf.disable());
              //  .apply(jwtConfigurer);
        ;
    }

    private AuthenticationSuccessHandler jsonAuthSuccessHandler() {
        return (req, res, auth) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.OK.value());
            res.getWriter().println(objectMapper.writeValueAsString(auth));
            logger.info("登录成功");
        };
    }

    private AuthenticationFailureHandler jsonAuthFailureHandler() {
        return (req, res, ex) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.UNAUTHORIZED.value());

            val errorData = Maps.of("title","登录失败").and("error", ex.getLocalizedMessage()).build();

            res.getWriter().println(objectMapper.writeValueAsString(errorData));
            logger.info("登录失败");
        };
    }


    private AccessDeniedHandler CustomAccessDeniedHandler() {

        return (HttpServletRequest req, HttpServletResponse res,
                AccessDeniedException ex) -> {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                logger.warn(String.format("User [%s] attempted to access the protected URL [%s]!", authentication.getName(), req.getRequestURI()));
            }

            //response.sendRedirect(request.getContextPath() + "/site/403");
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println(objectMapper.writeValueAsString(ex));

        };
    }


    /**
     * 权限不通过的处理
     */
    public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println("{ \"error\": \"" + authException.getMessage() + "\" }");

//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
//                    "Authentication Failed: " + authException.getMessage());
        }
    }


}
