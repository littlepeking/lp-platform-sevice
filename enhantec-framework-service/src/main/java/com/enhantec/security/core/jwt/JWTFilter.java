package com.enhantec.security.core.jwt;

import com.enhantec.security.common.models.EHAuthority;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTTokenProvider tokenProvider;

    private final JwtAuthFailureHandler jwtAuthFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = tokenProvider.resolveToken(httpServletRequest);
        if (StringUtils.hasText(jwt)) {
            Optional<Claims> claims = this.tokenProvider.getTokenClaims(jwt);
            if(claims.isPresent()) {
                ArrayList<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new EHAuthority( "ROLE_USER"));
                Authentication authentication = new UsernamePasswordAuthenticationToken(claims.get().getSubject(),"",authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                jwtAuthFailureHandler.onAuthenticationFailure((HttpServletRequest) servletRequest,(HttpServletResponse) servletResponse,new JwtAuthException("jwt token is invalid."));
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }


}
