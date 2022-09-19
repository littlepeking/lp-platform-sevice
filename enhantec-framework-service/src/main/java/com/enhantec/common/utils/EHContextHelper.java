package com.enhantec.common.utils;

import com.enhantec.Application;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class EHContextHelper {

    public static EHUser getUser(){
        return Application.getAppContext().getBean(EHUserDetailsService.class).getUserInfo(getAuthentication().getName());
    }

    public static Authentication getAuthentication(){
      return  SecurityContextHolder.getContext().getAuthentication();
    }
}
