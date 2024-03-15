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
package com.enhantec.framework.common.service;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.enhantec.framework.common.utils.DBHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
/***
 * Remove all multi datasource code as it should be specified by highest level services.
 */
public class EHSqlService {

//    @DS(DSConstants.DS_DEFAULT)
//    @Transactional(propagation = Propagation.REQUIRED)
    public List<Map<String, Object>> selectList(String statement, List<Object> params) {

        convertDateParams2SqlString(params);

        return SqlRunner.db().selectList(convertQuestionMark2Index(statement),params.toArray());
    }

    private static void convertDateParams2SqlString(List<Object> params) {
        for(int i = 0; i< params.size(); i++){
            if(params.get(i) instanceof LocalDateTime){
                params.set(i, DBHelper.convertLocalDateTime2SqlString((LocalDateTime)params.get(i)));
            }
        }
    }

    private static void convertDateParams2SqlString(Object[] params) {
        for(int i = 0; i< params.length; i++){
            if(params[i] instanceof LocalDateTime){
                params[i] = DBHelper.convertLocalDateTime2SqlString((LocalDateTime)params[i]);
            }
        }
    }

//    @DS(DSConstants.DS_PARAM)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public List<Map<String, Object>> selectList(String dataSource, String statement, List<Object> params) {
//        return SqlRunner.db().selectList(convertQuestionMark2Index(statement),params.toArray());
//    }

//    @DS(DSConstants.DS_DEFAULT)
//    @Transactional(propagation = Propagation.REQUIRED)
    public long selectCount(String statement, List<Object> params) {

        convertDateParams2SqlString(params);

        return SqlRunner.db().selectCount(convertQuestionMark2Index(statement),params.toArray());
    }

//    @DS(DSConstants.DS_PARAM)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public long selectCount(String dataSource, String statement, List<Object> params) {
//        return SqlRunner.db().selectCount(convertQuestionMark2Index(statement),params.toArray());
//    }

//    @DS(DSConstants.DS_DEFAULT)
    public Map<String, Object> selectOne(String statement, List<Object> params) {

        convertDateParams2SqlString(params);

        return SqlRunner.db().selectOne(convertQuestionMark2Index(statement),params.toArray());
    }

    public Object selectValue(String statement, List<Object> params) {

        convertDateParams2SqlString(params);

        return SqlRunner.db().selectObj(convertQuestionMark2Index(statement),params.toArray());
    }

    public Object selectValue(String statement, Object[] params) {

        convertDateParams2SqlString(params);

        return selectValue(statement, Arrays.asList(params));
    }




//    @DS(DSConstants.DS_PARAM)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public Map<String, Object> selectOne(String dataSource, String statement, List<Object> params) {
//        return SqlRunner.db().selectOne(convertQuestionMark2Index(statement),params.toArray());
//    }


//    @DS(DSConstants.DS_DEFAULT)
    public boolean executeUpdate(String statement, List<Object> params) {
        return doUpdate(statement, params);
    }


//    @DS(DSConstants.DS_PARAM)
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public boolean executeUpdate(String dataSource, String statement, List<Object> params) {
//        return doUpdate(statement, params);
//    }

    private boolean doUpdate(String statement, List<Object> params) {

        DBHelper.convertSqlParamListObj2String(params);

        if(statement.toLowerCase().trim().startsWith("insert into"))
            return  SqlRunner.db().insert(convertQuestionMark2Index(statement),params.toArray());
        else  if(statement.toLowerCase().trim().startsWith("update"))
            return  SqlRunner.db().update(convertQuestionMark2Index(statement),params.toArray());
        else  if(statement.toLowerCase().trim().startsWith("delete "))
            return  SqlRunner.db().delete(convertQuestionMark2Index(statement),params.toArray());
        else
            throw new UnsupportedOperationException("本方法不支持除 insert/update/delete 外的操作");
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
