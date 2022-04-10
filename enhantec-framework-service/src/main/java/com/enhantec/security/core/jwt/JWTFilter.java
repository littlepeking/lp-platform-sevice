package com.enhantec.security.core.jwt;

import com.enhantec.security.core.authentication.RestAuthFailureHandler;
import com.enhantec.security.core.authentication.RestAuthSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JWTFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final JWTTokenProvider tokenProvider;

    private final RestAuthFailureHandler jwtAuthFailureHandler;

    private final RestAuthSuccessHandler jwtAuthSuccessHandler;

    public JWTFilter(JWTTokenProvider tokenProvider, RestAuthSuccessHandler jwtAuthSuccessHandler, RestAuthFailureHandler jwtAuthFailureHandler) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthFailureHandler = jwtAuthFailureHandler;
        this.jwtAuthSuccessHandler = jwtAuthSuccessHandler;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest);
//        if (StringUtils.hasText(jwt)) {
//            if(this.tokenProvider.validateToken(jwt)) {
//                Authentication authentication = this.tokenProvider.getAuthentication(jwt);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }else {
//                jwtAuthFailureHandler.onAuthenticationFailure((HttpServletRequest) servletRequest,(HttpServletResponse) servletResponse,new JwtAuthException("jwt token is invalid."));
//                return;
//            }
//        }

        filterChain.doFilter(servletRequest, servletResponse);

    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
