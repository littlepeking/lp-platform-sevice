package com.enhantec.security.jwt;

import com.enhantec.config.properties.ApplicationProperties;
import com.enhantec.security.web.EHUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JWTTokenProvider {

    private final Logger log = LoggerFactory.getLogger(JWTTokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String SECRET = "YmJlMWVmMjYxODM1ZGVkMWI2MzA5M2UxOGJmMmEzMTYzYTg4ZDk3MTJkYjA1Mzc3YTI3YThlYjBhN2I3YTdlZjk1MThiMmMzYjE5NjYzMWFjYjdlMDZmODlhMDdhNDg3NGRjODg5ZWJlODg5NjYzNzljNDQ0MzllYjYwN2U5ODE=";

    private Key key;

    private long tokenValidityInMilliseconds;

    private final EHUserDetailsService userDetailsService;

    ApplicationProperties applicationProperties;

    public JWTTokenProvider(ApplicationProperties applicationProperties, EHUserDetailsService userDetailsService) {
        this.applicationProperties = applicationProperties;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        if (!StringUtils.isEmpty(applicationProperties.getSecurity().getJwt().getBase64Secret())) {
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(applicationProperties.getSecurity().getJwt().getBase64Secret());
        } else {
            log.debug("Using a Base64-encoded JWT secret key Default");
            keyBytes = Decoders.BASE64.decode(SECRET);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds =
            1000 * applicationProperties.getSecurity().getJwt().getTokenValidityInSeconds();

    }

    public String createToken(Authentication authentication) {
        return createToken(authentication, null);
    }

    /**
     * add params extPayload for trans more information
     * @param authentication
     * @param extPayLoad
     * @return
     */
    public String createToken(Authentication authentication, Map<String,Object> extPayLoad) {
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;

        validity = new Date(now + this.tokenValidityInMilliseconds);

        if (null != extPayLoad) {
            // add custom payload
            //extPayLoad.put(AUTHORITIES_KEY, authorities);
            return Jwts.builder()
                    .setSubject(authentication.getName())
                    .addClaims(extPayLoad)
                    .signWith(key, SignatureAlgorithm.HS512)
                    //.setExpiration(validity)
                    .compact();
        } else {
            return Jwts.builder()
                    .setSubject(authentication.getName())
                   // .claim(AUTHORITIES_KEY, authorities)
                    .signWith(key, SignatureAlgorithm.HS512)
                    //.setExpiration(validity)
                    .compact();
        }
    }

//    /**
//     * custom get user information form db and load extPayload
//     * @param token
//     * @return
//     */
//    public Authentication getAuthentication(String token) {
//        Claims claims = Jwts.parserBuilder()
//            .setSigningKey(key)
//            .build()
//            .parseClaimsJws(token)
//            .getBody();
//
//
//        /*
//        User principal = new User(claims.getSubject(), "", authorities);
//        */
//        // custom load more user information form db
//        JWTUser principal = (JWTUser)userDetailsService.loadUserByUsername(claims.getSubject());
//        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
//    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token.");

        }
        return false;
    }
}
