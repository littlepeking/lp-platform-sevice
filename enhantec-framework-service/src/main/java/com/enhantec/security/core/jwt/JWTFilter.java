package com.enhantec.security.core.jwt;

import com.enhantec.security.common.service.EHRoleService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTTokenProvider tokenProvider;

    private final JwtAuthFailureHandler jwtAuthFailureHandler;

    private final EHRoleService roleService;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        String jwt = tokenProvider.resolveToken(servletRequest);
        if (StringUtils.hasText(jwt)) {
            Optional<Claims> claims = this.tokenProvider.getTokenClaims(jwt);
            if(claims.isPresent()) {
                val roleList = roleService.findByUsername(claims.get().getSubject());

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(claims.get().getSubject(),"",roleList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                jwtAuthFailureHandler.onAuthenticationFailure((HttpServletRequest) servletRequest,(HttpServletResponse) servletResponse,new JwtAuthException("jwt token is invalid."));
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }


}
