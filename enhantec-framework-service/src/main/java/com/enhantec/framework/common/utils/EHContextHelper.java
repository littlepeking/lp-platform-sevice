/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.common.utils;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.config.EHRequestContextHolder;
import com.enhantec.framework.config.MultiDataSourceConfig;
import com.enhantec.framework.security.common.model.EHRole;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHUserDetailsService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EHContextHelper {


    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext){
        EHContextHelper.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType){
        return applicationContext.getBean(requiredType);
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    public static Class<? extends Object> getType(String name) {
        return applicationContext.getType(name);
    }

    public static HttpServletRequest getRequest(){
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();

        return  request;
    }

    public static String getCurrentOrgId(){

        return getBean(EHRequestContextHolder.class).getOrgId();

    }

    public static String getCurrentDataSource() {
        EHRequestContextHolder requestContextHolder = EHContextHelper.getBean(EHRequestContextHolder.class);
        return requestContextHolder.getDataSource();
    }


    public static String getDataSource(String orgId){
        return MultiDataSourceConfig.DATA_SOURCE_ORG_PREFIX+ orgId;
    }


    public static String getLanguageCode(){

        return getBean(EHRequestContextHolder.class).getLanguageCode();

    }

    public static EHUser getUser(){
        return applicationContext.getBean(EHUserDetailsService.class).getUserInfo(getAuthentication().getName());
    }

    public static List<EHRole> getRoles(){
        return (List<EHRole>) getUser().getRoles();
    }

    public static void checkUserPermissions(String[] authorities){

        if(authorities!=null && authorities.length > 0){

          Arrays.stream(authorities).forEach(auth->{
              long count = getRoles().stream().filter(r->r.getOrgId().equals(getCurrentOrgId())).map(r->r.getPermissions()).flatMap(Collection::stream).map(perm-> perm.getAuthority()).filter(permission -> permission.equals(auth)).count();
                if(count == 0) throw new EHApplicationException("s-auth-permissionDenied");
          });

        }

    }

    public static Authentication getAuthentication(){
      return SecurityContextHolder.getContext().getAuthentication();
    }
}
