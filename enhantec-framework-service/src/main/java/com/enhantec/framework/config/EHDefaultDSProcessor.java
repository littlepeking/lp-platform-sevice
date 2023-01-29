/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.config;

import com.baomidou.dynamic.datasource.processor.DsHeaderProcessor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.processor.DsSessionProcessor;
import com.baomidou.dynamic.datasource.processor.DsSpelExpressionProcessor;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.common.utils.EHContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EHDefaultDSProcessor extends DsProcessor {
    @Override
    public boolean matches(String key) {
        return key.equals(DSConstants.DS_DEFAULT);
    }

    @Override
    public String doDetermineDatasource(MethodInvocation invocation, String key) {
       // RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            //if job then we get attribute in requestContextBean from EHRequestAttributes
            EHRequestContextHolder requestContextHolder = EHContextHelper.getBean(EHRequestContextHolder.class);
            return requestContextHolder.getDataSource();
            //Object val = (RequestContextHolder.getRequestAttributes()).getAttribute(key.substring(9), RequestAttributes.SCOPE_REQUEST);
            //return  val==null? null: val.toString();

    }


    @Bean
    public DsProcessor dsProcessor(BeanFactory beanFactory) {
        EHDefaultDSProcessor ehDefaultDSProcessor = new EHDefaultDSProcessor();
        DsHeaderProcessor headerProcessor = new DsHeaderProcessor();
        DsSessionProcessor sessionProcessor = new DsSessionProcessor();
        DsSpelExpressionProcessor spelExpressionProcessor = new DsSpelExpressionProcessor();
        spelExpressionProcessor.setBeanResolver(new BeanFactoryResolver(beanFactory));
        ehDefaultDSProcessor.setNextProcessor(headerProcessor);
        headerProcessor.setNextProcessor(sessionProcessor);
        sessionProcessor.setNextProcessor(spelExpressionProcessor);
        return ehDefaultDSProcessor;
    }

}
