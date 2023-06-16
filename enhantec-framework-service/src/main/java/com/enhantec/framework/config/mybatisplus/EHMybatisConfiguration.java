/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.config.mybatisplus;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;

public class EHMybatisConfiguration extends MybatisConfiguration {

    @Override
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement,
                                                RowBounds rowBounds, ParameterHandler parameterHandler,
                                                ResultHandler resultHandler, BoundSql boundSql) {
        return new EHMybatisResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler,
                boundSql, rowBounds);
    }
}