/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.common.utils;

import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class EHContextHelper {


    private static ApplicationContext appContext;

    public static void setApplicationContext(ApplicationContext appContext){
        EHContextHelper.appContext = appContext;
    }

    public static <T> T getBean(Class<T> requiredType){
        return appContext.getBean(requiredType);
    }

    public static HttpServletRequest getRequest(){
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();

        return  request;
    }

    public static String getCurrentOrgId(){

        return  getRequest().getHeader("orgId");

    }

    public static String getLanguageCode(){

        return  getRequest().getHeader("Accept-Language");

    }

    public static EHUser getUser(){
        return appContext.getBean(EHUserDetailsService.class).getUserInfo(getAuthentication().getName());
    }

    public static Authentication getAuthentication(){
      return  SecurityContextHolder.getContext().getAuthentication();
    }
}
