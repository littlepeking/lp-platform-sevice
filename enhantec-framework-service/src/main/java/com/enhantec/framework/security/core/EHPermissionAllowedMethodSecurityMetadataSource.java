/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.security.core;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.access.annotation.Jsr250SecurityConfig.DENY_ALL_ATTRIBUTE;

public class EHPermissionAllowedMethodSecurityMetadataSource  extends AbstractFallbackMethodSecurityMetadataSource {
        @Override
        protected Collection findAttributes(Class<?> clazz) { return null; }

        @Override
        protected Collection findAttributes(Method method, Class<?> targetClass) {
            Annotation[] annotations = AnnotationUtils.getAnnotations(method);
            List attributes = new ArrayList<>();

//            boolean isControllerClass = false;

            // if the class is annotated as @Controller we should by default deny access to all methods
            if (AnnotationUtils.findAnnotation(targetClass, Controller.class) != null) {
                //TODO:当开启如下开关将默认禁止访问未标明权限的方法，但会导致在访问不存在的的URL时，AbstractSecurityInterceptor在执行beforeInvocation时Authentication对象为空
                // （这是由于此时SecurityContextPersistenceFilter此时已经完成SecurityContextHolder.clearContext()，
                // 而正常的代码执行过程应该是先执行AbstractSecurityInterceptor.beforeInvocation，再执行SecurityContextPersistenceFilter的上述逻辑），
                // 系统报An Authentication object was not found in the SecurityContext，而非错误代码404，此部分待后续优化。在没有好的办法前可先注释下面这行以禁用默认禁止访问无annotation的逻辑。
               // attributes.add(DENY_ALL_ATTRIBUTE);
//                isControllerClass = true;
            }

            if (annotations != null) {
                for (Annotation a : annotations) {
                    // but not if the method has at least a PreAuthorize or PostAuthorize annotation
                    if (a instanceof PreAuthorize || a instanceof PostAuthorize) {
                        return null;
                    }
                }
            }

//            if(isControllerClass && Arrays.stream(annotations).filter(a->a instanceof RequestMapping).count()>0)
//            throw new RuntimeException(targetClass.getName()+"存在未配置权限的方法" +method.getName());

            return attributes;
        }

        @Override
        public Collection getAllConfigAttributes() { return null; }
    }



