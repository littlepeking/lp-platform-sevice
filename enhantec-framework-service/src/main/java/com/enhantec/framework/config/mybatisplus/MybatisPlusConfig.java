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



package com.enhantec.framework.config.mybatisplus;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@MapperScan("com.enhantec.**.mapper")
@Configuration
public class MybatisPlusConfig {

//    @Bean
//    public MybatisConfiguration mybatisConfig() {
//        MybatisConfiguration configuration = new MybatisConfiguration();
//        configuration.getTypeHandlerRegistry().register(LocalDateTime.class, new LocalDateTimeTypeHandler());
//        return configuration;
//    }

    //as we introduced ConfigurationCustomizer, so have to remove this bean to avoid conflict
//    @Bean
//    public MybatisConfiguration mybatisConfiguration() {
//        return new LocalDateTimeConfiguration();
//    }



}
