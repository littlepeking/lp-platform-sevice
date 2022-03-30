package com.enhantec.security.web;

import com.enhantec.security.core.properties.SecurityProperties;
import com.enhantec.security.web.support.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class WebSecurityController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    //Get original request before redirect to authentication service
    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private final SecurityProperties securityProperties;

    public WebSecurityController(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @RequestMapping("/authentication/require")
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public SimpleResponse forward2LoginPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SavedRequest savedRequest = requestCache.getRequest(request,response);

        if(savedRequest!=null) {
            String targetUrl = savedRequest.getRedirectUrl();
            logger.info("original url:"+ targetUrl);
            if(StringUtils.endsWithIgnoreCase(targetUrl,".html")){
                redirectStrategy.sendRedirect(request,response,securityProperties.getWebProperties().getLoginPage());
            }
        }
        return new SimpleResponse("访问的服务需要身份验证");
    }

}
