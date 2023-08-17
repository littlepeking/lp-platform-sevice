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

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class EHMybatisResultSetHandler extends DefaultResultSetHandler {

    public EHMybatisResultSetHandler(Executor executor, MappedStatement mappedStatement,
                                     ParameterHandler parameterHandler, ResultHandler<?> resultHandler,
                                     BoundSql boundSql, RowBounds rowBounds) {
        super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    }

    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        List<Object> resultList = super.handleResultSets(stmt);
        if (resultList != null) {
            for (Object result : resultList) {
                if (result instanceof Map) {
                    handleMapResult((Map<String, Object>) result);
                }
            }
        }
        return resultList;
    }

    //As mysql will use localDateTime as default return type, but sqlserver return timeStamp as return type.
    // For consistent purpose, we need make sure localDateTime will be always returned, so we convert timestamp to localDateTime here
    private void handleMapResult(Map<String, Object> resultMap) {
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            Object columnValue = entry.getValue();
            if (columnValue instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) columnValue;
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                entry.setValue(localDateTime);
            }
        }
    }

}