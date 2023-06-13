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



package com.enhantec.framework.config;

import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiDataSourceConfig {
    @Value("${spring.datasource.dynamic.datasource.admin.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.dynamic.datasource.admin.url}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.admin.orgUrlTemplate}")
    private String orgUrlTemplate;
    @Value("${spring.datasource.dynamic.datasource.admin.username}")
    private String username;
    @Value("${spring.datasource.dynamic.datasource.admin.password}")
    private String password;

    public static String DATA_SOURCE_ORG_PREFIX = "ORG__";


    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new AbstractJdbcDataSourceProvider(driverClassName, url, username, password) {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement)
                    throws SQLException {
                Map<String, DataSourceProperty> map = new HashMap<>();

                ResultSet rs = statement.executeQuery("select * from EH_ORGANIZATION");
                while (rs.next()) {
                    if(rs.getString("DB_NAME")!=null) {
                        String orgId = rs.getString("ID");
                        String dbName = rs.getString("DB_NAME");
                        DataSourceProperty property = new DataSourceProperty();
                        property.setUsername(username);
                        property.setPassword(password);
                        property.setUrl(String.format(orgUrlTemplate, dbName));
                        property.setDriverClassName(driverClassName);
                        map.put(DATA_SOURCE_ORG_PREFIX+orgId, property);
                        log.info("===load datasource {}===", orgId);
                    }

                }
                return map;
            }
        };
    }

}
