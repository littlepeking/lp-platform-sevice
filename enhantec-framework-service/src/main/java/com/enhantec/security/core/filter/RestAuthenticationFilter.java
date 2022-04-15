package com.enhantec.security.core.filter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.services.EHUserDetailsService;
import com.enhantec.security.common.services.EHUserService;
import com.enhantec.security.core.enums.AuthType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Locale;

@RequiredArgsConstructor
public class RestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    private final EHUserDetailsService userDetailsService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //if authentication type is LDAP, throw org.springframework.ldap.AuthenticationException();

        UsernamePasswordAuthenticationToken authToken;

        try {
            InputStream is = request.getInputStream();
            val jsonNode = objectMapper.readTree(is);

            String username = jsonNode.get("username").textValue();
            String password = jsonNode.get("password").textValue();

            String lowercaseLogin = username.toLowerCase(Locale.ENGLISH);
            authToken = new UsernamePasswordAuthenticationToken(lowercaseLogin,password);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("Cannot find username or password.");
        }

        setDetails(request,authToken);

        return this.getAuthenticationManager().authenticate(authToken);

    }
}
