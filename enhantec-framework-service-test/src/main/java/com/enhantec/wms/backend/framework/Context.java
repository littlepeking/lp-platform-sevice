package com.enhantec.wms.backend.framework;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Context {

    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public Connection getConnection() {
        return  null;
    }

    public void releaseStatement(Statement statement) {
    }

    public void releaseResultSet(ResultSet resultSet) {
    }

    public void releaseConnection(Connection connection) {
    }

}
