package com.enhantec.security.web;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.jwt.JWTConfigurer;
import com.enhantec.security.web.filter.RestAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
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
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;

import static com.enhantec.security.Constants.*;

@RequiredArgsConstructor
@Slf4j
@EnableWebSecurity()
@Order(99)
public class RestAPISecurityConfig extends WebSecurityConfigurerAdapter {

    //private Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationProperties applicationProperties;

    JWTConfigurer jwtConfigurer;

    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final SecurityProblemSupport securityProblemSupport;

    private final DataSource dataSource;

    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().mvcMatchers("/error/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("12345678")).roles("USER", "ADMIN");
//        auth.inMemoryAuthentication().withUser("john").password(passwordEncoder.encode("12345678")).roles("USER");

        auth.jdbcAuthentication()
                //.withDefaultSchema()
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


        http.
                httpBasic(Customizer.withDefaults()).
                requestMatchers(rm-> rm.antMatchers(

                        authUrl,swaggerUrl,
                        "/api/**",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/code/image"
                        ))
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req->req.antMatchers(authUrl,swaggerUrl,
                        "/swagger-resources/**", "/v3/api-docs", "/code/image").permitAll().anyRequest().authenticated())
                //REPLACE UsernamePasswordAuthenticationFilter WITH RestAuthenticationFilter
                .addFilterAt(getRestAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class)
                //.exceptionHandling(exHandler -> exHandler.accessDeniedHandler(customAccessDeniedHandler()))
                // .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .exceptionHandling(exHandler -> exHandler.authenticationEntryPoint(securityProblemSupport).
                        accessDeniedHandler(securityProblemSupport))
                .csrf(csrf -> csrf.disable())
        //  .apply(jwtConfigurer);
        ;
    }

    private RestAuthenticationFilter getRestAuthenticationFilter() throws Exception {
        RestAuthenticationFilter restAuthenticationFilter = new RestAuthenticationFilter(objectMapper);
        restAuthenticationFilter.setAuthenticationSuccessHandler(jsonAuthSuccessHandler());
        restAuthenticationFilter.setAuthenticationFailureHandler(jsonAuthFailureHandler());
        restAuthenticationFilter.setAuthenticationManager(authenticationManager());
        restAuthenticationFilter.setFilterProcessesUrl(authUrl);
        return restAuthenticationFilter;
    }

    private AuthenticationSuccessHandler jsonAuthSuccessHandler() {
        return (req, res, auth) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.OK.value());
            res.getWriter().println(objectMapper.writeValueAsString(auth));
            log.info("登录成功");
        };
    }

    private AuthenticationFailureHandler jsonAuthFailureHandler() {
        return (req, res, ex) -> {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpStatus.UNAUTHORIZED.value());

            val errorData = Maps.of("title", "登录失败").and("details", ex.getLocalizedMessage()).build();

            res.getWriter().println(objectMapper.writeValueAsString(errorData));
            log.info("登录失败");
        };
    }


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
