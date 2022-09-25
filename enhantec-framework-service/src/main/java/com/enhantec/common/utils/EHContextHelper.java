package com.enhantec.common.utils;

import com.enhantec.Application;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class EHContextHelper {

    public static <T> T getBean(Class<T> requiredType){
        return Application.getAppContext().getBean(requiredType);
    }

    public static HttpServletRequest getRequest(){
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();

        return  request;
    }

    public static String getLanguageCode(){

        return  getRequest().getHeader("Accept-Language");

    }

    public static EHUser getUser(){
        return Application.getAppContext().getBean(EHUserDetailsService.class).getUserInfo(getAuthentication().getName());
    }

    public static Authentication getAuthentication(){
      return  SecurityContextHolder.getContext().getAuthentication();
    }
}
