package com.enhantec.wms.backend.utils.common;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.UserInfo;

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


    public static <T> T getValue(Context context, Connection connection, String sql, Object[] params, Class<T> type, String errorName) throws DBResourceException {

        return getValue(context,connection,sql,params, type, errorName,true);

    }

    public static <T> T getValue(Context context,Connection connection, String sql, Object[] params, Class<T> type, String errorName,boolean checkExist) throws DBResourceException{

        String val = getValue(context,connection,sql,params, errorName,checkExist);

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

    public static String getValue(Context context,Connection connection, String sql, Object[] params,String errorName) throws DBResourceException{

        HashMap<String,String> record = getRecord(context,connection,sql,params, errorName,true);

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return  (String) record.values().toArray()[0];

    }

    public static String getValue(Context context,Connection connection, String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        HashMap<String,String> record = getRecord(context,connection,sql,params, errorName,checkExist);
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

    public static Object getRawValue(Context context,Connection connection, String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        HashMap record = getRawRecord(context,connection,sql,params, errorName);
        if(null == record) return null;

        if(record.values().size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'结果为一列，当前为"+record.size()+"列");

        return record.values().toArray()[0];

    }


    /**
     * 返回一列多行的数据。
     * @param context
     * @param connection
     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static List getValueList(Context context,Connection connection,String sql,Object[] params,String errorName)throws DBResourceException{
        List result = new ArrayList();
        List<HashMap<String, String>> list = executeQuery(context, connection, sql, params);
        for (HashMap<String, String> map : list) {
            if(map.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询"+errorName+"结果为一列，当前为"+map.size()+"列");
            result.add(map.values().toArray()[0]);
        }
        return result;
    }

    public static HashMap<String,String> getRecordByConditions(Context context,Connection connection, String tableName, HashMap<String,Object> conditionParams,String errorName) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        List<HashMap<String,String>>  list = executeQuery(context,connection, tableName,conditionParams);

        if(list.size()==0)  return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }


    /**
     *  return null if record cannot found
     * @param context
     * @param connection
     * @param sql
     * @param params
     * @param errorName
     * @return
     * @throws DBResourceException
     */
    public static HashMap<String,String> getRecord(Context context,Connection connection, String sql, Object[] params,String errorName) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(context,connection,sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'的数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    public static HashMap<String,String> getRecord(Context context,Connection connection, String sql, Object[] params,String errorName,boolean checkExist) throws DBResourceException{

        List<HashMap<String,String>> list = executeQuery(context,connection,sql, Arrays.asList(params));

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


    public static HashMap<String,Object> getRawRecord(Context context,Connection connection, String sql, Object[] params,String errorName) throws DBResourceException{

        List<HashMap<String,Object>> list = executeQueryRawData(context,connection,sql, Arrays.asList(params));

        if(list.size()==0) return null;
        if(list.size()!=1) ExceptionHelper.throwRfFulfillLogicException("期望查询'"+errorName+"'数据为一行，当前为"+list.size()+"行");
        return list.get(0);

    }

    public static List<HashMap<String,String>> executeQuery(Context context, Connection connection, String sql, Object[] params) throws DBResourceException{

        return executeQuery(context,connection,sql, Arrays.asList(params));

    }

    public static List<HashMap<String,String>> executeQuery(Context context, Connection connection, String tableName, HashMap<String,Object> conditionParams) throws DBResourceException{

        if(conditionParams == null || conditionParams.size()==0) ExceptionHelper.throwRfFulfillLogicException("查询条件不能为空");

        StringBuffer whereClauseSB = new StringBuffer();
        conditionParams.entrySet().stream().forEach(entry->whereClauseSB.append(entry.getKey() + " = ? and "));
        String whereClause = whereClauseSB.substring(0, whereClauseSB.length() - 4);

        return executeQuery(context,connection,"select * from "+tableName+" where "+ whereClause, conditionParams.values());

    }

    public static List<HashMap<String,String>> executeQuery(Context context, Connection connection, String sql, Collection<Object> params) throws DBResourceException{

        PreparedStatement qqPrepStmt1 = null;
        ResultSet qqResultSet1 = null;

        try {

            if(connection == null || connection.isClosed()) connection = context.getConnection();
            qqPrepStmt1 = connection.prepareStatement(sql);

            int i=0;
            for(Object p : params){
                DBHelper.setValue(qqPrepStmt1, ++i, p);
            }

            qqResultSet1 = qqPrepStmt1.executeQuery();

            return resultSetToArrayList(qqResultSet1);

        } catch (SQLException qqSQLException) {
            SQLException e1=new SQLException(qqSQLException.getMessage());
            throw new DBResourceException(e1);
        } finally {
            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
        }

    }

    public static List<HashMap<String,String>> executeQuery(UserInfo userInfo, String sql, Collection<Object> params) throws DBResourceException{

        Connection connection = null;
        PreparedStatement qqPrepStmt1 = null;
        ResultSet qqResultSet1 = null;

        try {
            connection = userInfo.context.getConnection();
            qqPrepStmt1 = connection.prepareStatement(sql);

            int i=0;
            for(Object p : params){
                DBHelper.setValue(qqPrepStmt1, ++i, p);
            }

            qqResultSet1 = qqPrepStmt1.executeQuery();

            return resultSetToArrayList(qqResultSet1);

        } catch (SQLException qqSQLException) {
            SQLException e1=new SQLException(qqSQLException.getMessage());
            throw new DBResourceException(e1);
        } finally {
            DBHelper.release(userInfo, qqResultSet1,qqPrepStmt1,connection);
        }

    }

    public static List<HashMap<String,Object>> executeQueryRawData(Context context, Connection connection, String sql, Collection<Object> params) throws DBResourceException{

        PreparedStatement qqPrepStmt1 = null;
        ResultSet qqResultSet1 = null;

        try {

            if(connection==null|| connection.isClosed()) connection = context.getConnection();
            qqPrepStmt1 = connection.prepareStatement(sql);

            int i=0;
            for(Object p : params){
                DBHelper.setValue(qqPrepStmt1, ++i, p);
            }

            qqResultSet1 = qqPrepStmt1.executeQuery();

            return resultSetToRawArrayList(qqResultSet1);


        } catch (SQLException qqSQLException) {
            SQLException e1=new SQLException(qqSQLException.getMessage());
            throw new DBResourceException(e1);
        } finally {
            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
        }
    }

    //未完成，待实现
    @Deprecated
    public static void executeUpdate(Context context, Connection connection, String tableName, HashMap<String,Object> updateParams,  HashMap<String,Object> whereParams) throws DBResourceException{

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

        executeUpdate(context,connection,sqlStringBuffer.toString(),params);

    }

    public static void executeUpdate(Context context, Connection connection, String sql, Object[] params) throws DBResourceException {

        List<Object> paramList =  Arrays.asList(params);
        executeUpdate(context,connection,sql,paramList);

    }

    public static void executeUpdate(Context context, Connection connection, String sql, List<Object> params) throws DBResourceException{

        PreparedStatement qqPrepStmt1 = null;
        ResultSet qqResultSet1 = null;

        try {

            if(connection==null || connection.isClosed()) connection = context.getConnection();
            qqPrepStmt1 = connection.prepareStatement(sql);

            int i=0;
            for(Object p : params){
                DBHelper.setValue(qqPrepStmt1, ++i, p);
            }

            qqPrepStmt1.executeUpdate();


        } catch (SQLException qqSQLException) {
            SQLException e1=new SQLException(qqSQLException.getMessage());
            throw new DBResourceException(e1);
        } finally {
            DBHelper.release(context, qqResultSet1,qqPrepStmt1,connection);
//            context.releaseStatement(qqPrepStmt1);
//            context.releaseResultSet(qqResultSet1);
//            context.releaseConnection(connection);
        }


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

    public static ArrayList<HashMap<String,String>> resultSetToArrayList(ResultSet rs) throws SQLException{

        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList<HashMap<String,String>> list = new ArrayList<>();
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

    public static void release(Context context, Statement statement){
        try { if(statement!=null ) context.releaseStatement(statement);  } catch (Exception e) {}
    }


    public static void release(Context context, ResultSet resultSet, Statement statement , Connection connection){
        try { if(resultSet!=null ) context.releaseResultSet(resultSet); } catch (Exception e) {}
        try { if(statement!=null ) context.releaseStatement(statement);  } catch (Exception e) {}
        try { if(connection!=null ) context.releaseConnection(connection);  } catch (Exception e) {}
    }


    public static void release(UserInfo userInfo, ResultSet resultSet, Statement statement , Connection connection){
        try { if(resultSet!=null ) userInfo.context.releaseResultSet(resultSet); } catch (Exception e) {}
        try { if(statement!=null ) userInfo.context.releaseStatement(statement);  } catch (Exception e) {}
        try { if(connection!=null ) userInfo.context.releaseConnection(connection);  } catch (Exception e) {}
    }


    public static void releaseNativeConnection(Connection connection){
        try { if(connection!=null )  connection.close();  } catch (Exception e) {}
    }


    public static Object getValue(ResultSet qqResultSet, int i) {
       throw new RuntimeException("not implement");
    }
}
