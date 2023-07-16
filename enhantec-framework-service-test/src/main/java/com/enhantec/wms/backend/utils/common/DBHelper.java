package com.enhantec.wms.backend.utils.common;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.UserInfo;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
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


    public static <T> T getValue(Context context, String sql, Object[] params, Class<T> type, String errorName) throws DBResourceException {

        return getValue(context,sql,params, type, errorName,true);

    }

    public static <T> T getValue(Context context,String sql, Object[] params, Class<T> type, String errorName,boolean checkExist) throws DBResourceException{

        String val = getValue(context,sql,params, errorName,checkExist);

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

    public static String getValue(Context context,String sql, Object[] params) throws DBResourceException{

        HashMap<String,String> record = getRecord(context,sql,params);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+record.size()+"列");

        return  (String) record.values().toArray()[0];

    }

    public static String getValue(Context context,String sql, Object[] params,String errorName) throws DBResourceException{

        HashMap<String,String> record = getRecord(context,sql,params, errorName,true);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return  (String) record.values().toArray()[0];

    }

    public static String getValue(Context context,String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        HashMap<String,String> record = getRecord(context,sql,params, errorName,checkExist);
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return (String) record.values().toArray()[0];

    }


    public static String getValue(UserInfo userInfo, String sql, Object[] params, String errorName, boolean checkExist) throws DBResourceException{

        HashMap<String,String> record = getRecord(userInfo,sql,params, errorName,checkExist);

        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return (String) record.values().toArray()[0];

    }

    public static String getValue(UserInfo userInfo, String sql, Object[] params,String errorName) throws DBResourceException{

        HashMap<String,String> record = getRecord(userInfo,sql,params, errorName,true);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return record.values().stream().findFirst().get();

    }

    public static Object getRawValue(Context context,String sql, Object[] params) throws DBResourceException{

        HashMap record = getRawRecord(context,sql,params, "");
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+record.size()+"列");

        return record.values().toArray()[0];

    }


    public static Object getRawValue(Context context,String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        HashMap record = getRawRecord(context,sql,params, errorName);
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return record.values().toArray()[0];

    }


    /**
     * 返回一列多行的数据。
     * @param context
     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static List getValueList(Context context, String sql,Object[] params,String errorName)throws DBResourceException{
        List result = new ArrayList();
        List<HashMap<String, String>> list = executeQuery(context, sql, params);
        for (HashMap<String, String> map : list) {
            if(map.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询"+errorName+"结果为一列，当前为"+map.size()+"列");
            result.add(map.values().toArray()[0]);
        }
        return result;
    }


    /**
     *  only used for refector legacy code which without error msg.
     */
    @Deprecated
    public static List getValueList(Context context, String sql,Object[] params)throws DBResourceException{
        List result = new ArrayList();
        List<HashMap<String, String>> list = executeQuery(context, sql, params);
        for (HashMap<String, String> map : list) {
            if(map.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询结果为一列，当前为"+map.size()+"列");
            result.add(map.values().toArray()[0]);
        }
        return result;
    }

    public static HashMap<String,String> getRecordByConditions(Context context,String tableName, HashMap<String,Object> conditionParams,String errorName) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        List<HashMap<String,String>>  list = executeQuery(context, tableName,conditionParams);

        if(list.size()==0)  return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }



    /**
     *  only used for refector legacy code which without error msg.
     */
    @Deprecated
    public static HashMap<String,String> getRecord(Context context,String sql, Object[] params) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(context,sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    /**
     *  return null if record cannot found
     * @param context
     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static HashMap<String,String> getRecord(Context context,String sql, Object[] params,String errorName) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(context,sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    public static HashMap<String,String> getRecord(Context context,String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(context,sql, Arrays.asList(params));

        if(list.size()==0){
            if(checkExist) ExceptionHelper.throwRfFulfillLogicException("未找到"+errorName);
            else return null;
        }

        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    public static HashMap<String,String> getRecord(UserInfo userInfo, String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(userInfo,sql, Arrays.asList(params));

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
    public static HashMap<String,Object> getRawRecord(Context context,String sql, Object[] params) throws DBResourceException{

       return getRawRecord(context,sql,params,"");

    }

    public static HashMap<String,Object> getRawRecord(Context context,String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<HashMap<String,Object>> list = executeQueryRawData(context,sql, Arrays.asList(params));

        if(list.size()==0){
            if(checkExist) ExceptionHelper.throwRfFulfillLogicException("未找到"+errorName);
            else return null;
        }

        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }


    public static HashMap<String,Object> getRawRecord(Context context,String sql, Object[] params,String errorName) throws DBResourceException{

       return getRawRecord(context,sql,params,errorName, true);

    }

    public static List<HashMap<String,String>> executeQuery(Context context, String sql, Object[] params) throws DBResourceException{

        return executeQuery(context,sql, Arrays.asList(params));

    }

    public static List<HashMap<String,String>> executeQuery(Context context, String tableName, HashMap<String,Object> conditionParams) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        StringBuffer whereClauseSB = new StringBuffer();
        conditionParams.entrySet().stream().forEach(entry->whereClauseSB.append(entry.getKey() + " = ? and "));
        String whereClause = whereClauseSB.substring(0, whereClauseSB.length() - 4);

        return executeQuery(context,"select * from "+tableName+" where "+ whereClause, conditionParams.values());

    }

//    public static List<HashMap<String,String>> executeQuery(Context context, String sql, Collection<Object> params) throws DBResourceException{
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
//            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
//        }
//
//    }
    

    public static List<HashMap<String, String>> executeQuery(Context context, String sql, Collection<Object> params) {
        return  context.getJdbcTemplate().query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                int index = 1;
                for (Object param : params) {
                    preparedStatement.setObject(index, param);
                    index++;
                }
            }
        }, new ResultSetExtractor<List<HashMap<String, String>>>() {
            @Override
            public List<HashMap<String, String>> extractData(ResultSet rs) throws SQLException {
                List<HashMap<String, String>> rows = new ArrayList<>();
                while (rs.next()) {
                    HashMap<String, String> row = new HashMap<>();
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnLabel(i);
                        String columnValue = rs.getString(i);
                        row.put(columnName, columnValue);
                    }
                    rows.add(row);
                }
                return rows;
            }
        });
    }


    public static List<HashMap<String,String>> executeQuery(UserInfo userInfo, String sql, Collection<Object> params){

        return  executeQuery(userInfo.context,sql,params);

    }

//    public static List<HashMap<String,Object>> executeQueryRawData(Context context, String sql, Collection<Object> params){
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
//            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
//        }
//    }

    public static List<HashMap<String, Object>> executeQueryRawData(Context context, String sql, Collection<Object> params) {
        return context.getJdbcTemplate().query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                int index = 1;
                for (Object param : params) {
                    preparedStatement.setObject(index, param);
                    index++;
                }
            }
        }, new RowMapper<HashMap<String, Object>>() {
            @Override
            public HashMap<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                HashMap<String, Object> row = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object columnValue = rs.getObject(i);
                    row.put(columnName, columnValue);
                }
                return row;
            }
        });
    }

    //未完成，待实现
    @Deprecated
    public static void executeUpdate(Context context, String tableName, HashMap<String,Object> updateParams,  HashMap<String,Object> whereParams) throws DBResourceException{

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

        executeUpdate(context,sqlStringBuffer.toString(),params);

    }
//
//    public static void executeUpdate(Context context, String sql, List<Object> params) throws DBResourceException{
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
//            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
////            context.releaseStatement(qqPrepStmt1);
////            context.releaseResultSet(qqResultSet1);
////            context.releaseConnection(connection);
//        }
//
//
//    }

    public static void executeUpdate(Context context, String sql, List<Object> params) {
        context.getJdbcTemplate().update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                int index = 1;
                for (Object param : params) {
                    preparedStatement.setObject(index, param);
                    index++;
                }
            }
        });
    }

    public static void executeUpdate(Context context, String sql, Object[] params) throws DBResourceException {

        List<Object> paramList =  Arrays.asList(params);
        executeUpdate(context,sql,paramList);

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

    public static List<HashMap<String,String>> resultSetToArrayList(ResultSet rs) throws SQLException{

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<HashMap<String,String>> list = new ArrayList<>();
        while (rs.next()){
            HashMap row = new HashMap(columns);
            for(int i=1; i<=columns; ++i){
                row.put((md.getColumnName(i)).toUpperCase(),rs.getObject(i)==null ? null : rs.getObject(i).toString());
            }
            list.add(row);
        }

        return list;

    }

    public static ArrayList<HashMap<String,Object>> resultSetToRawArrayList(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList<HashMap<String,Object>> list = new ArrayList<>();
        while (rs.next()){
            HashMap row = new HashMap(columns);
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
