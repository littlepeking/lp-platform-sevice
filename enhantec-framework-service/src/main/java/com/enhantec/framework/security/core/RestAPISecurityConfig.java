/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.core;

import com.enhantec.framework.config.properties.ApplicationProperties;
import com.enhantec.framework.security.Constants;
import com.enhantec.framework.security.core.filter.ApiKeyFilter;
import com.enhantec.framework.security.core.filter.JWTFilter;
import com.enhantec.framework.security.core.jwt.JWTTokenProvider;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHUserDetailsService;
import com.enhantec.framework.security.common.service.JWTCacheService;
import com.enhantec.framework.security.common.service.RoleHierarchyService;
import com.enhantec.framework.security.core.enums.AuthType;
import com.enhantec.framework.security.core.ldap.BasicAuthenticationProvider;
import com.enhantec.framework.security.core.ldap.FailedAuthenticationProvider;
import com.enhantec.framework.security.core.ldap.LDAPAuthenticationProvider;
import com.enhantec.framework.security.core.filter.RestAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
@EnableWebSecurity()
@Order(99)
public class RestAPISecurityConfig extends WebSecurityConfigurerAdapter {

    //private Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationProperties applicationProperties;

    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProblemSupport securityProblemSupport;

    private final EHUserDetailsService ehUserDetailsService;

    private final LDAPAuthenticationProvider ldapAuthenticationProvider;

    private final BasicAuthenticationProvider basicAuthenticationProvider;

    private final FailedAuthenticationProvider failedAuthenticationProvider;

    private final JWTTokenProvider jwtTokenProvider;

    private final ApiKeyFilter apiKeyFilter;

    private final JWTFilter jwtFilter;

    private final Environment environment;

    private final RoleHierarchyService roleHierarchyService;

    private final JWTCacheService jwtCacheService;

    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().mvcMatchers("/error/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("12345678")).roles("USER", "ADMIN");
//        auth.inMemoryAuthentication().withUser("john").password(passwordEncoder.encode("12345678")).roles("USER");

        //Replace jdbcAuthentication with customized EHUserDetailsService
        if(applicationProperties.getSecurity().getAuthTypes().contains(AuthType.BASIC.toString())) {
            auth.authenticationProvider(basicAuthenticationProvider);
        }
        if(applicationProperties.getSecurity().getAuthTypes().contains(AuthType.LDAP.toString())) {
            auth.authenticationProvider(ldapAuthenticationProvider);
        }
        auth.authenticationProvider(failedAuthenticationProvider);





//        auth.jdbcAuthentication()
//                //.withDefaultSchema()
//                .dataSource(dataSource)
//                .usersByUsernameQuery("select username,password,enabled,login_name from users where username = ?")
//                .authoritiesByUsernameQuery("select username,authority from authorities where username = ?");
//                .withUser("admin")
//                .password(passwordEncoder.encode("1234"))
//                .roles("ADMIN");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
//        validateCodeFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
//        validateCodeFilter.setApplicationProperties(applicationProperties);
//        validateCodeFilter.afterPropertiesSet();


        http.
                //httpBasic(Customizer.withDefaults()).  //allow auth from http header
                requestMatchers(rm-> rm.antMatchers(
                        Constants.authUrl, Constants.swaggerUrl,
                        "/api/**",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/code/image"
                        ))
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req->req.antMatchers(Constants.authUrl, Constants.changePasswordUrl, Constants.swaggerUrl,
                        "/swagger-resources/**", "/v3/api-docs", "/code/image"
                        // ,"/api/test-receipt/**"
                ).permitAll().anyRequest().authenticated())
                //REPLACE UsernamePasswordAuthenticationFilter WITH RestAuthenticationFilter
                .addFilterBefore(apiKeyFilter,UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(getRestAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class)
                //.exceptionHandling(exHandler -> exHandler.accessDeniedHandler(customAccessDeniedHandler()))
                // .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .exceptionHandling(exHandler -> exHandler.authenticationEntryPoint(securityProblemSupport).
                        accessDeniedHandler(securityProblemSupport))
                .cors(cors-> cors.configurationSource(configurationSource()))
                .csrf(csrf -> csrf.disable())

        //  .apply(jwtConfigurer);
        ;
    }
//
//    @Bean
//    BasicAuthenticationProvider basicAuthenticationProvider(){
//        val basicAuthenticationProvider = new BasicAuthenticationProvider();
//        basicAuthenticationProvider.setUserDetailsService(ehUserDetailsService);
//        basicAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//        return  basicAuthenticationProvider;
//    }



    private RestAuthenticationFilter getRestAuthenticationFilter() throws Exception {
        RestAuthenticationFilter restAuthenticationFilter = new RestAuthenticationFilter(objectMapper,ehUserDetailsService);
        restAuthenticationFilter.setAuthenticationSuccessHandler(jsonAuthSuccessHandler());
        restAuthenticationFilter.setAuthenticationFailureHandler(jsonAuthFailureHandler());
        restAuthenticationFilter.setAuthenticationManager(authenticationManager());
        restAuthenticationFilter.setFilterProcessesUrl(Constants.authUrl);
        return restAuthenticationFilter;
    }

    AuthenticationSuccessHandler jsonAuthSuccessHandler() {
        return (req, res, auth) -> {
            // custom payload
            HashMap<String, Object> extPayLoad = new HashMap<>();
            EHUser user = (EHUser) auth.getPrincipal();
            // jwt token
            String jwt = jwtTokenProvider.createToken(auth, extPayLoad);

            jwtCacheService.addOrRenewToken(jwt,req.getRemoteAddr());
            jwtCacheService.addOrRenewUserToken(user.getId(),jwt);
            log.info( req.getRemoteAddr());
            //AuthDto authDto = new AuthDto(auth.getName(),jwt);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.OK.value());
            res.setHeader(JWTTokenProvider.AUTHORIZATION_HEADER,"Bearer " + jwt);
            res.getWriter().println(objectMapper.writeValueAsString(auth.getPrincipal()));
            log.info("登录成功");
        };
    }

    AuthenticationFailureHandler jsonAuthFailureHandler() {
        return (req, res, ex) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.UNAUTHORIZED.value());

            val errorData = Maps.of("title", "登录失败").and("details", ex.getLocalizedMessage()).build();

            res.getWriter().println(objectMapper.writeValueAsString(errorData));
            log.info("登录失败");
        };
    }



    /**
     * Replaced by problem lib
     */
    @Deprecated
    private AccessDeniedHandler customAccessDeniedHandler() {

        return (HttpServletRequest req, HttpServletResponse res,
                AccessDeniedException ex) -> {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                log.warn(String.format("User [%s] attempted to access the protected URL [%s]!", authentication.getName(), req.getRequestURI()));
            }

            //response.sendRedirect(request.getContextPath() + "/site/403");
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().println(objectMapper.writeValueAsString(ex));

        };
    }


    /**
     * Replaced by problem lib
     */

    @Deprecated
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

    @Bean
    CorsConfigurationSource configurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        if(environment.acceptsProfiles(Profiles.of("dev"))) {
            //corsConfiguration.addAllowedOrigin("http://localhost:*"); // ui service -dev
            corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        }else {
            corsConfiguration.addAllowedOrigin("http://localhost_prd:8080");// ui service -prd
        }

        corsConfiguration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.addExposedHeader("X-Authenticate");//RESPONSE ADD HEADER X-Authenticate TO ALLOW 2 FACTOR AUTH, NOT USE NOW.
        corsConfiguration.addExposedHeader("authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);
        return source;

    }




}
