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
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.enhantec.framework.common.utils.DSConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.orgUrlTemplate}")
    private String orgUrlTemplate;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public static String DATA_SOURCE_ORG_PREFIX = "ORG__";


    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new AbstractJdbcDataSourceProvider(driverClassName, url, username, password) {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement)
                    throws SQLException {

                Map<String, DataSourceProperty> map = new HashMap<>();

                //Add admin datasource
                DataSourceProperty propertyAdmin = new DataSourceProperty();
                propertyAdmin.setUsername(username);
                propertyAdmin.setPassword(password);
                propertyAdmin.setUrl(url);
                propertyAdmin.setDriverClassName(driverClassName);
                map.put(DSConstants.DS_ADMIN, propertyAdmin);
                log.info("Loaded SQLServer datasource for admin");
                ////////

                //Add ORG datasources
                ResultSet rs = statement.executeQuery("select * from EH_ORGANIZATION");
                if(driverClassName.contains("sqlserver")){

                        while (rs.next()) {
                            String connectionStringParams = rs.getString("CONNECTION_STRING_PARAMS");

                            if (connectionStringParams != null) {
                                String[] paramsArray = connectionStringParams.split(";;;");

                                if (paramsArray.length != 2)
                                    throw new RuntimeException("Database connection string parameters is incorrect: SQLServer need 2 parameters, string format should be: username;;;password. Parameter value is: " + connectionStringParams);

                                String orgId = rs.getString("ID");
                                DataSourceProperty property = new DataSourceProperty();
                                property.setUsername(paramsArray[0]);
                                property.setPassword(paramsArray[1]);

                                if (StringUtils.isEmpty(orgUrlTemplate)) {
                                    orgUrlTemplate = url;
                                }

                                property.setUrl(orgUrlTemplate);
                                property.setDriverClassName(driverClassName);
                                map.put(DATA_SOURCE_ORG_PREFIX + orgId, property);
                                log.info("Loaded SQLServer datasource for org {} ", orgId);
                            }
                        }
                }else if(driverClassName.contains("mysql")){
                        String connectionStringParams = rs.getString("CONNECTION_STRING_PARAMS");
                        while (rs.next()) {
                        if(connectionStringParams !=null) {
                            String orgId = rs.getString("ID");
                            DataSourceProperty property = new DataSourceProperty();
                            property.setUsername(username);
                            property.setPassword(password);
                            property.setUrl(String.format(orgUrlTemplate,connectionStringParams));
                            property.setDriverClassName(driverClassName);
                            map.put(DATA_SOURCE_ORG_PREFIX+orgId, property);
                            log.info("Loaded mysql datasource for org {} ", orgId);
                        }
                    }


                }
                return map;
            }
        };
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //For define boolean column: sqlserver => bit, mysql => TINYINT(1)
        if(driverClassName.contains("sqlserver")) {
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQL_SERVER2005));
        }else if(driverClassName.contains("mysql")){
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        }
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;

    }

}
