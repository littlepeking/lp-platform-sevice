package com.enhantec.wms.backend.utils.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.enhantec.framework.common.service.EHSqlService;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.common.utils.EHContextHelper;
import org.mybatis.spring.SqlSessionTemplate;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import static com.enhantec.sce.common.Const.DateTimeFormat;

public class DBHelper {

//    public static DateTimeNullable UtilHelper.convertStringToSqlDate(String dateString){
//
//        DateTimeNullable d = new DateTimeNullable();
//
//        if(UtilHelper.isEmpty(dateString)){
//            d.setNull(true);
//        }else{
//            d.setValue(dateString,new DateFormat(DateTimeFormat));
//        }
//
//        return d;
//    }


    public static <T> T getValue( String sql, Object[] params, Class<T> type, String errorName) throws DBResourceException {

        return getValue(sql,params, type, errorName,true);

    }

    public static <T> T getValue(String sql, Object[] params, Class<T> type, String errorName,boolean checkExist) throws DBResourceException{

        String val = getValue(sql,params, errorName,checkExist);

        if(val == null ) {
            return null;
        }else {
            if (String.class.isAssignableFrom(type)) return (T) val;
            else if (BigDecimal.class.isAssignableFrom(type)) return (T) new BigDecimal(val);
            else if (Integer.class.isAssignableFrom(type)) return (T) Integer.valueOf(val);
        }
        ExceptionHelper.throwRfFulfillLogicException("不支持从字符串向"+type+"的类型转换");
        return null;
    }

    public static String getValue(String sql, Object[] params) throws DBResourceException{

        Map<String,String> record = getRecord(sql,params);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+record.size()+"列");

        return  (String) record.values().toArray()[0];

    }

    public static String getValue(String sql, Object[] params,String errorName) throws DBResourceException{

        Map<String,String> record = getRecord(sql,params, errorName,true);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return  (String) record.values().toArray()[0];

    }

    public static String getValue(String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        Map<String,String> record = getRecord(sql,params, errorName,checkExist);
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return (String) record.values().toArray()[0];

    }


    public static Object getRawValue(String sql, Object[] params) throws DBResourceException{

        Map record = getRawRecord(sql,params, "");
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+record.size()+"列");

        return record.values().toArray()[0];

    }


    public static Object getRawValue(String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        Map record = getRawRecord(sql,params, errorName);
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return record.values().toArray()[0];

    }


    /**
     * 返回一列多行的数据。

     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static List getValueList( String sql,Object[] params,String errorName)throws DBResourceException{
        List result = new ArrayList();
        List<Map<String, String>> list = executeQuery( sql, params);
        for (Map<String, String> map : list) {
            if(map.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询"+errorName+"结果为一列，当前为"+map.size()+"列");
            result.add(map.values().toArray()[0]);
        }
        return result;
    }


    /**
     *  only used for refector legacy code which without error msg.
     */
    @Deprecated
    public static List getValueList( String sql,Object[] params)throws DBResourceException{
        List result = new ArrayList();
        List<Map<String, String>> list = executeQuery( sql, params);
        for (Map<String, String> map : list) {
            if(map.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+map.size()+"列");
            result.add(map.values().toArray()[0]);
        }
        return result;
    }

    public static Map<String,String> getRecordByConditions(String tableName, Map<String,Object> conditionParams,String errorName) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        List<Map<String,String>>  list = executeQuery( tableName,conditionParams);

        if(list.size()==0)  return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }



    /**
     *  only used for refector legacy code which without error msg.
     */
    @Deprecated
    public static Map<String,String> getRecord(String sql, Object[] params) throws DBResourceException{

        List<Map<String,String>> list = executeQuery(sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    /**
     *  return null if record cannot found

     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static Map<String,String> getRecord(String sql, Object[] params,String errorName) throws DBResourceException{

        List<Map<String,String>> list = executeQuery(sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    public static Map<String,String> getRecord(String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<Map<String,String>> list = executeQuery(sql, Arrays.asList(params));

        if(list.size()==0){
            if(checkExist) ExceptionHelper.throwRfFulfillLogicException("未找到"+errorName);
            else return null;
        }

        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    /**
     *  only used for refactor legacy code which without error msg.
     */
    @Deprecated
    public static Map<String,Object> getRawRecord(String sql, Object[] params) throws DBResourceException{

       return getRawRecord(sql,params,"");

    }

    public static Map<String,Object> getRawRecord(String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<Map<String,Object>> list = executeQueryRawData(sql, Arrays.asList(params));

        if(list.size()==0){
            if(checkExist) ExceptionHelper.throwRfFulfillLogicException("未找到"+errorName);
            else return null;
        }

        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }


    public static Map<String,Object> getRawRecord(String sql, Object[] params,String errorName) throws DBResourceException{

       return getRawRecord(sql,params,errorName, true);

    }

    public static List<Map<String,String>> executeQuery( String sql, Object[] params) throws DBResourceException{

        return executeQuery(sql, Arrays.asList(params));

    }

    public static List<Map<String,String>> executeQuery( String tableName, Map<String,Object> conditionParams) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        StringBuffer whereClauseSB = new StringBuffer();
        List params  = new ArrayList();
        conditionParams.entrySet().stream().forEach(entry->{
            whereClauseSB.append(entry.getKey() + " = ? and ");
            params.add(entry.getValue());
        });
        String whereClause = whereClauseSB.substring(0, whereClauseSB.length() - 4);

        return executeQuery("select * from "+tableName+" where "+ whereClause, params);

    }

//    public static List<Map<String,String>> executeQuery( String sql, Collection<Object> params) throws DBResourceException{
//
//        PreparedStatement qqPrepStmt1 = null;
//        ResultSet qqResultSet1 = null;
//
//        try {
//
//            if(connection == null || connection.isClosed()) connection = context.getConnection();
//            qqPrepStmt1 = DBHelper.executeUpdate(sql);
//
//            int i=0;
//            for(Object p : params){
//                DBHelper.setValue(qqPrepStmt1, ++i, p);
//            }
//
//            qqResultSet1 = qqPrepStmt1.executeQuery();
//
//            return resultSetToArrayList(qqResultSet1);
//
//        } catch (SQLException qqSQLException) {
//            SQLException e1=new SQLException(qqSQLException.getMessage());
//            throw new DBResourceException(e1);
//        } finally {
//            DBHelper.release( qqResultSet1,qqPrepStmt1,connection);
//        }
//
//    }


    private static List<Map<String, String>> convertList(List<Map<String, Object>> originalList) {
        List<Map<String, String>> convertedList = new ArrayList<>();

        // Iterate over each element in the original list
        for (Map<String, Object> originalMap : originalList) {
            // Create a new Map to store the converted values
            Map<String, String> convertedMap = new HashMap<>();

            // Iterate over each entry in the original Map
            for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Convert the value from Object to String
                String convertedValue = value ==null? null : value.toString();

                // Add the converted key-value pair to the converted Map
                convertedMap.put(key, convertedValue);
            }

            // Add the converted Map to the converted list
            convertedList.add(convertedMap);
        }

        return convertedList;
    }

    public static List<Map<String, String>> executeQuery(String sql, List<Object> params) {

        List<Map<String, Object>> res = executeQueryRawData(EHContextHelper.getCurrentDataSource(),sql,params);
        return convertList(res);
    }

//    public static List<Map<String,Object>> executeQueryRawData( String sql, Collection<Object> params){
//
//        PreparedStatement qqPrepStmt1 = null;
//        ResultSet qqResultSet1 = null;
//
//        try {
//
//            if(connection==null|| connection.isClosed()) connection = context.getConnection();
//            qqPrepStmt1 = DBHelper.executeUpdate(sql);
//
//            int i=0;
//            for(Object p : params){
//                DBHelper.setValue(qqPrepStmt1, ++i, p);
//            }
//
//            qqResultSet1 = qqPrepStmt1.executeQuery();
//
//            return resultSetToRawArrayList(qqResultSet1);
//
//
//        } catch (SQLException qqSQLException) {
//            SQLException e1=new SQLException(qqSQLException.getMessage());
//            throw new DBResourceException(e1);
//        } finally {
//            DBHelper.release( qqResultSet1,qqPrepStmt1,connection);
//        }
//    }

    //未完成，待实现
    @Deprecated
    public static void executeUpdate( String tableName, Map<String,Object> updateParams,  Map<String,Object> whereParams) throws DBResourceException{

        if(updateParams==null || updateParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("更新失败：表名或更新字段列表不允许为空");
        if(whereParams==null || whereParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("更新失败：WHERE条件字段类表不允许为空");


        StringBuffer sqlStringBuffer = new StringBuffer("Update ");
        sqlStringBuffer.append(tableName);
        sqlStringBuffer.append(" set ");
        updateParams.entrySet().stream().map(e-> sqlStringBuffer.append(" " + e.getKey()+ " = ? ," ));
        sqlStringBuffer.append(" where ");
        whereParams.entrySet().stream().map(e-> sqlStringBuffer.append(" " + e.getKey()+ " = ? AND" ));

        List<Object> params = Stream.concat(updateParams.values().stream(), whereParams.values().stream())
                .collect(Collectors.toList());

        executeUpdate(sqlStringBuffer.toString(),params);

    }
//
//    public static void executeUpdate( String sql, List<Object> params) throws DBResourceException{
//
//        PreparedStatement qqPrepStmt1 = null;
//        ResultSet qqResultSet1 = null;
//
//        try {
//
//            if(connection==null || connection.isClosed()) connection = context.getConnection();
//            qqPrepStmt1 = DBHelper.executeUpdate(sql);
//
//            int i=0;
//            for(Object p : params){
//                DBHelper.setValue(qqPrepStmt1, ++i, p);
//            }
//
//            qqPrepStmt1.executeUpdate();
//
//
//        } catch (SQLException qqSQLException) {
//            SQLException e1=new SQLException(qqSQLException.getMessage());
//            throw new DBResourceException(e1);
//        } finally {
//            DBHelper.release( qqResultSet1,qqPrepStmt1,connection);
////            context.releaseStatement(qqPrepStmt1);
////            context.releaseResultSet(qqResultSet1);
////            context.releaseConnection(connection);
//        }
//
//
//    }

    public static void executeUpdate( String sql, List<Object> params) {

        executeUpdate(EHContextHelper.getCurrentDataSource(),sql, params);
    }



    public static void executeUpdate( String sql, Object[] params) throws DBResourceException {

        List<Object> paramList = Arrays.asList(params);
        executeUpdate(sql,paramList);

    }

    public static Map<String, Object> selectOne(String dataSourceKey, String statement, Map<String, Object> params) {
        try{
            SqlSessionTemplate sqlSessionTemplate = getSqlSessionTemplate();
            sqlSessionTemplate.getConnection().setSchema(dataSourceKey);
            return sqlSessionTemplate.selectOne(statement, params);
        }catch (SQLException e){
            throw new DBResourceException(e.getNextException());
        }
    }


    public static List<Map<String, Object>> executeQueryRawData(String statement, List<Object> params) {

            return  EHContextHelper.getBean(EHSqlService.class).selectList(statement,params);

    }

    public static List<Map<String, Object>> executeQueryRawData(String dataSource, String statement, List<Object> params) {

        return  EHContextHelper.getBean(EHSqlService.class).selectList(dataSource, statement,params);

    }

    public static List<Map<String, Object>> executeQueryRawDataByOrgId(String orgId, String statement, List<Object> params) {

        return  EHContextHelper.getBean(EHSqlService.class).selectList(EHContextHelper.getDataSource(orgId), statement,params);

    }





    public static List<Map<String, Object>> selectList(String dataSourceKey, String statement, Map<String, Object> params) {
        try{
            SqlSessionTemplate sqlSessionTemplate = getSqlSessionTemplate();
            sqlSessionTemplate.getConnection().setSchema(dataSourceKey);
            return sqlSessionTemplate.selectList(statement, params);
        }catch (SQLException e){
            throw new DBResourceException(e.getNextException());
        }
    }

    public static int executeUpdate(String dataSourceKey, String statement, List<Object> params) {

        try {
            SqlSessionTemplate sqlSessionTemplate = getSqlSessionTemplate();
            sqlSessionTemplate.getConnection().setSchema(dataSourceKey);
            return sqlSessionTemplate.update(statement, params);
        }catch (SQLException e){
            throw new DBResourceException(e.getNextException());
        }

    }

    public static int executeUpdate(String dataSourceKey, String statement, Map<String, Object> params) throws SQLException {
        try{
            SqlSessionTemplate sqlSessionTemplate = getSqlSessionTemplate();
            sqlSessionTemplate.getConnection().setSchema(dataSourceKey);
            return sqlSessionTemplate.update(statement, params);
        }catch (SQLException e){
            throw new DBResourceException(e.getNextException());
        }
    }

    private static SqlSessionTemplate getSqlSessionTemplate() {
        return EHContextHelper.getBean(SqlSessionTemplate.class);
    }


    public static void setValue(PreparedStatement sqlStatement, int position, Object value) {

        try {
//            if (value == null) {
//              sqlStatement.setNull(position, java.sql.Types.NULL);
//            }else if(value instanceof Byte[]) {
//                InputStream stream = new ByteArrayInputStream((byte[])value);
//                sqlStatement.setBinaryStream(position, stream);
//            } else if(value instanceof String) {
//                sqlStatement.setString(position, (String) value);
//            }else if(value instanceof Boolean) {
//                sqlStatement.setBoolean(position, (Boolean) value);
//            }else if(value instanceof Date) {
//                Timestamp ts = new Timestamp(((Date)value).getTime());
//                sqlStatement.setTimestamp(position, ts);
//            }else if(value instanceof java.sql.Date) {
//                Timestamp ts = new Timestamp(((java.sql.Date)value).getTime());
//                sqlStatement.setTimestamp(position, ts);
//            }else if(value instanceof BigDecimal) {
//                sqlStatement.setBigDecimal(position, (BigDecimal)value);
//            }else if(value instanceof Short) {
//                sqlStatement.setShort(position, (Short)value);
//            }else if(value instanceof Float) {
//                sqlStatement.setFloat(position, (Float)value);
//            }else if(value instanceof Double) {
//                sqlStatement.setDouble(position, (Double)value);
//            }else if(value instanceof Integer) {
//                sqlStatement.setInt(position, (Integer)value);
//            }else{
//                sqlStatement.setObject(position, value);
//            }


            sqlStatement.setObject(position, value);

        } catch (SQLException exception) {
            throw new FulfillLogicException(exception);
        }


    }

    public static List<Map<String,String>> resultSetToArrayList(ResultSet rs) throws SQLException{

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String,String>> list = new ArrayList<>();
        while (rs.next()){
            Map row = new HashMap(columns);
            for(int i=1; i<=columns; ++i){
                row.put((md.getColumnName(i)).toUpperCase(),rs.getObject(i)==null ? null : rs.getObject(i).toString());
            }
            list.add(row);
        }

        return list;

    }

    public static ArrayList<Map<String,Object>> resultSetToRawArrayList(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        while (rs.next()){
            Map row = new HashMap(columns);
            for(int i=1; i<=columns; ++i){
                row.put((md.getColumnName(i)).toUpperCase(),rs.getObject(i)==null ? null : rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }


    public static Object getValue(ResultSet qqResultSet, int i) {
       throw new RuntimeException("not implement");
    }
}
