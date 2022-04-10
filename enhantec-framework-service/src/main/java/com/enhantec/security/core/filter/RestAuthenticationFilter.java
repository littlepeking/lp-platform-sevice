package com.enhantec.security.core.filter;

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

@RequiredArgsConstructor
public class RestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        UsernamePasswordAuthenticationToken authToken;

        try {
            InputStream is = request.getInputStream();
            val jsonNode = objectMapper.readTree(is);

            String username = jsonNode.get("username").textValue();
            String password = jsonNode.get("password").textValue();
            authToken = new UsernamePasswordAuthenticationToken(username,password);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("Cannot find username or password.");
        }

        setDetails(request,authToken);

        return this.getAuthenticationManager().authenticate(authToken);

    }
}
