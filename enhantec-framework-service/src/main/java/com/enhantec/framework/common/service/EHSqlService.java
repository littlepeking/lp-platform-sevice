/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.enhantec.framework.common.utils.DSConstants;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EHSqlService {

    @DS(DSConstants.DS_PARAM)
    public List<Map<String, Object>> selectList(String dataSource, String statement, List<Object> params) {
        return  SqlRunner.db().selectList(convertQuestionMark2Index(statement),params.toArray());
    }
    @DS(DSConstants.DS_DEFAULT)
    public List<Map<String, Object>> selectList(String statement, List<Object> params) {
        return  SqlRunner.db().selectList(convertQuestionMark2Index(statement),params.toArray());
    }



    private  static String convertQuestionMark2Index(String sql) {
        // Regular expression pattern to match question marks
        Pattern questionMarkPattern = Pattern.compile("\\?");

        // Matcher object to find question marks in the SQL query
        Matcher questionMarkMatcher = questionMarkPattern.matcher(sql);

        // Check if there are any question marks in the SQL query
        if (!questionMarkMatcher.find()) {
            // No question marks found, return the original SQL query as-is
            return sql;
        }

        StringBuilder convertedSql = new StringBuilder();
        int parameterIndex = 0;

        // Loop through the SQL query, finding question marks and replacing them with parameter index
        do {
            // Append the part of the SQL query before the question mark
            convertedSql.append(sql.substring(0, questionMarkMatcher.start()));

            // Append the parameter index
            convertedSql.append("{"+parameterIndex+"}");

            // Move the starting point of the substring to the end of the question mark
            sql = sql.substring(questionMarkMatcher.end());

            // Increment the parameter index for the next question mark
            parameterIndex++;

            // Update the matcher with the updated SQL
            questionMarkMatcher = questionMarkPattern.matcher(sql);
        } while (questionMarkMatcher.find());

        // Append the remaining part of the SQL query after the last question mark
        convertedSql.append(sql);

        return convertedSql.toString();
    }

}
