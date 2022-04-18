package com.enhantec.security.core.jwt;

import com.enhantec.config.properties.ApplicationProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JWTTokenProvider {

    private final Logger log = LoggerFactory.getLogger(JWTTokenProvider.class);



    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORITIES_KEY = "authorities";
    private static final String SECRET = "YmJlMWVmMjYxODM1ZGVkMWI2MzA5M2UxOGJmMmEzMTYzYTg4ZDk3MTJkYjA1Mzc3YTI3YThlYjBhN2I3YTdlZjk1MThiMmMzYjE5NjYzMWFjYjdlMDZmODlhMDdhNDg3NGRjODg5ZWJlODg5NjYzNzljNDQ0MzllYjYwN2U5ODE=";

    private Key key;

    private long tokenValidityInMilliseconds;
    private final ApplicationProperties applicationProperties;

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
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        if (null != extPayLoad) {
            // add custom payload
            extPayLoad.put(AUTHORITIES_KEY, authorities);
            return Jwts.builder()
                    .setSubject(authentication.getName())
                    .addClaims(extPayLoad)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .setIssuedAt(new Date(now))
                    //.setExpiration(validity)
                    .compact();
        } else {
            return Jwts.builder()
                    .setSubject(authentication.getName())
                   // .claim(AUTHORITIES_KEY, authorities)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .setIssuedAt(new Date(now))
                    //.setExpiration(validity)
                    .compact();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    public Optional<Claims> getTokenClaims(String authToken) {
        try {
            return Optional.of(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).getBody());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token.");
            return  Optional.empty();

        }
    }

}
