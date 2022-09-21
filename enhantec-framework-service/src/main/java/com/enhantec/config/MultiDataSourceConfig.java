package com.enhantec.config;

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
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.orgUrlTemplate}")
    private String orgUrlTemplate;
    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String username;
    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String password;


    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new AbstractJdbcDataSourceProvider(driverClassName, url, username, password) {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement)
                    throws SQLException {
                Map<String, DataSourceProperty> map = new HashMap<>();

                ResultSet rs = statement.executeQuery("select * from EH_ORGANIZATION");
                while (rs.next()) {
                    String orgId = rs.getString("ID");
                    String dbName = rs.getString("DB_NAME");
                    DataSourceProperty property = new DataSourceProperty();
                    property.setUsername(username);
                    property.setPassword(password);
                    property.setUrl(String.format(orgUrlTemplate, dbName));
                    property.setDriverClassName(driverClassName);
                    map.put(orgId, property);
                    log.info("load datasource {}", orgId);
                }
                return map;
            }
        };
    }

}
