package com.enhantec.wms.backend.framework;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Context {

    private String userID;

    private JdbcTemplate jdbcTemplate;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSchema(){
        throw  new RuntimeException("no implement");
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){

        this.jdbcTemplate =jdbcTemplate;

    }
    public JdbcTemplate getJdbcTemplate(){
        return jdbcTemplate;
    }


    //public Connection getConnection() {
//        return  null;
//    }

    public void releaseStatement(Statement statement) {
    }

    public void releaseResultSet(ResultSet resultSet) {
    }

    public void releaseConnection(Connection connection) {
    }

}
