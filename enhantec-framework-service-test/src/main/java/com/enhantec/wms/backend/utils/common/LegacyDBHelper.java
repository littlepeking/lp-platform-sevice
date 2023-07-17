package com.enhantec.wms.backend.utils.common;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.config.MultiDataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import java.util.*;

@Deprecated
public class LegacyDBHelper {

	public static boolean Isdebug=true;
//	public static String DateFormat="yyyy/MM/dd";
//	public static String DateTimeFormat="yyyy/MM/dd HH:mm:ss";//MM/dd/yyyy hh24:mi:ss

	public static String DateFormat="MM/dd/yyyy";
	public static String DateTimeFormat="MM/dd/yyyy HH:mm:ss";//MM/dd/yyyy hh24:mi:ss


	private static Logger sqlLogger = LoggerFactory.getLogger(LegacyDBHelper.class);


	public static void ExecInsert( String TableName, HashMap<String,String> Fields) throws Exception
	{
		ArrayList<String> aParams=new ArrayList<String>();
		String SQL1="insert into "+TableName+"(";
		String SQL2="values(";
		int iLine=0;
		for(Map.Entry<String, String> entry : Fields.entrySet()){
			String mapKey = entry.getKey();
			String mapValue = entry.getValue();
			if (iLine>0) SQL1+=",";
			SQL1+=mapKey;
			if (iLine>0) SQL2+=",";
			if (LegecyUtilHelper.Nz(mapValue,"").equals("@user"))
			{
				SQL2+=" CURRENT_USER ";
			}
			else
			{
				SQL2+="? ";
				aParams.add(mapValue);
			}
			iLine++;
		}
		DBHelper.executeUpdate( SQL1+") "+SQL2+")",aParams.toArray());
	}

	public static void ExecInsert(String orgId, String TableName, HashMap<String,String> Fields) throws Exception
	{
		List<Object> aParams=new ArrayList<Object>();
		String SQL1="insert into "+TableName+"(";
		String SQL2="values(";
		int iLine=0;
		for(Map.Entry<String, String> entry : Fields.entrySet()){
			String mapKey = entry.getKey();
			String mapValue = entry.getValue();
			if (iLine>0) SQL1+=",";
			SQL1+=mapKey;
			if (iLine>0) SQL2+=",";
			if (LegecyUtilHelper.Nz(mapValue,"").equals("@user"))
			{
				SQL2+=" CURRENT_USER ";
			}
			else
			{
				SQL2+="? ";
				aParams.add(mapValue);
			}
			iLine++;
		}
		DBHelper.executeUpdate(EHContextHelper.getDataSource(orgId), SQL1+") "+SQL2+")",aParams);
	}




	public static void ExecDelete(String TableName,HashMap<String,String> Fields) throws Exception
	{
		ArrayList<String> aParams=new ArrayList<String>();
		String SQL="delete "+TableName+" where ";
		int iLine=0;
		for(Map.Entry<String, String> entry : Fields.entrySet()){
			if (iLine>0) SQL+=" and ";
			String mapKey = entry.getKey();
			String mapValue = entry.getValue();
			if (LegecyUtilHelper.Nz(mapValue,"").equals("@user"))
			{
				SQL+=mapKey+"= USER ";
			}
			else
			{
				SQL+=mapKey+"= ? ";
				aParams.add(mapValue);
			}
			iLine++;
		}
		DBHelper.executeUpdate( SQL,aParams.toArray());
	}


	public static void ExecUpdate(String TableName,HashMap<String,String> UpdateFields,HashMap<String,String> WhereFields) throws Exception
	{
		ArrayList<String> aParams=new ArrayList<String>();
		String SQL="update "+TableName+" set ";
		int iUpdateLine=0;
		for(Map.Entry<String, String> UpdateEntry : UpdateFields.entrySet()){
			if (iUpdateLine>0) SQL+=" , ";
			String mapKey = UpdateEntry.getKey();
			String mapValue = UpdateEntry.getValue();
			if (LegecyUtilHelper.isNull2(mapValue))
			{
				SQL+=mapKey+" = null ";
			}
			else
			{
				if (LegecyUtilHelper.Nz(mapValue,"").equals("@user"))
				{
					SQL+=mapKey+"= USER ";
				}
				else
				{
					SQL+=mapKey+"= ? ";
					aParams.add(mapValue);
				}
			}
			iUpdateLine++;
		}
		SQL+=" where ";
		int iWhereLine=0;
		for(Map.Entry<String, String> WhereEntry : WhereFields.entrySet()){
			if (iWhereLine>0) SQL+=" and ";
			String mapKey = WhereEntry.getKey();
			String mapValue = WhereEntry.getValue();
			if (LegecyUtilHelper.isNull2(mapValue))
			{
				SQL+=mapKey+" is null ";
			}
			else
			{
				if (LegecyUtilHelper.Nz(mapValue,"").equals("@user"))
				{
					SQL+=mapKey+"= USER ";
				}
				else
				{
					SQL+=mapKey+"= ? ";
					aParams.add(mapValue);
				}
			}
			iWhereLine++;
		}
		DBHelper.executeUpdate( SQL,aParams.toArray());
	}


	public static void ExecSql(String Sql,List<String> Params) throws Exception
	{
		String[] aParams=new String[Params.size()];
		for(int i1=0;i1<Params.size();i1++) aParams[i1]=Params.get(i1);
		DBHelper.executeUpdate( Sql,aParams);
	}


//	public static int ExecSql(String Sql,String[] Params) throws Exception
//	{
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--ExecSql--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------ExecSql---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//
//		}
//
//		PreparedStatement sm = null;
//		try
//		{
//
//			sm = conn.prepareStatement(Sql);
//			if (Params!=null)
//				for(int i1=0;i1<Params.length;i1++)
//					setParamValue(sm,i1+1,Params[i1]);
//			int i1=sm.executeUpdate();
//			return i1;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}
//		finally {
//			DBHelper.release(sm);
//		}
//	}


//	public static int ExecSqlByObject(String Sql,Object[] Params) throws Exception
//	{
//
//		PreparedStatement sm = null;
//		try
//		{
//
//			sm = conn.prepareStatement(Sql);
//			if (Params!=null)
//				for(int i1=0;i1<Params.length;i1++)
//					sm.setObject(i1+1,Params[i1]);
//			int i1=sm.executeUpdate();
//
//			return i1;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(sm);
//		}
//	}

//
//	public static int GetCount(String Sql,String[] Params) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetValue--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//			sqlLogger.info("-------------------GetValue---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		int Result=0;
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			rs=sm.executeQuery();
//			if (rs.next())
//			{
//				Result= rs.getInt(1);
//			}
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(rs,sm);
//		}
//	}
//
//	public static String GetValue(String Sql,String[] Params,String DefValue) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetValue--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------GetValue---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		String Result=null;
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			rs=sm.executeQuery();
//			if (rs.next())
//			{
//				Result= rs.getString(1);
//			}
//			if (Result==null) Result=DefValue;
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(rs,sm);
//		}
//	}


	public static int GetSeq(String Seq) throws Exception
	{

//    	Statement sm = null;
//    	ResultSet rs = null;
//    	int Result=0;
//    	try
//    	{
//    		sm = conn.createStatement();
//			rs = sm.executeQuery("Select "+Seq+".nextval as SEQ From Dual");
//			if (rs.next())
//			{
//				Result= rs.getInt(1);
//			}
//			else
//				throw new Exception("ncounter ("+Seq+") load error");
//	  		try	{ if (rs!=null) context.releaseResultSet(rs); } catch(Exception e1){
//    			try{ rs.close();}  catch(Exception e2){}
//    		}
//
//       		try	{ if (sm!=null)  context.releaseStatement(sm);  } catch(Exception e1){
//    			try{ sm.close();}  catch(Exception e2){}
//    		}			return Result;
//    	}
//    	catch(Exception e0)
//    	{
//	  		try	{ if (rs!=null) context.releaseResultSet(rs); } catch(Exception e1){
//    			try{ rs.close();}  catch(Exception e2){}
//    		}
//
//       		try	{ if (sm!=null)  context.releaseStatement(sm);  } catch(Exception e1){
//    			try{ sm.close();}  catch(Exception e2){}
//    		}
//       		throw e0;
//    	}

		return 0;
	}

//
//	public static String GetNCounterBill(EXEBaseStep frombean,String KeyName) throws Exception
//	{
//		if (frombean==null)
//			return GetNCounterBill("System",KeyName);
//		else
//			return frombean.getNextKey(KeyName, 10, context).getAsString();
//	}
//

	public static String GetNCounterBill(String KeyName) throws Exception
	{
		return GetNCounterBill("System",KeyName);
	}


	public static String GetNCounterBill(String UserID,String KeyName) throws Exception
	{
		int iBill=GetNCounter(UserID,KeyName,1,1);
		String Bill=Integer.toString(iBill);
		while(Bill.length()<10) Bill="0"+Bill;
		return Bill;
	}

	public static int GetNCounter(String KeyName,int Count,int FirstValue) throws Exception
	{
		return GetNCounter("System",KeyName,Count,FirstValue);
	}

	public static int GetNCounter(String UserID,String KeyName,int Count,int FirstValue) throws Exception
	{

		//todo get seq

		throw new RuntimeException("not implement");



//
//		PreparedStatement sm = null;
//		PreparedStatement sm2 = null;
//		ResultSet rs = null;
//		int Result=0;
//		try
//		{
//			if (Count<=0)  throw new Exception("Not find ("+KeyName+") by ncounter");
//			sm = conn.prepareStatement("Select KeyCount from ncounter where KeyName=?");
//			if (context!=null)
//				DBHelper.setValue(sm, 1, KeyName.toUpperCase());
//			else
//				sm.setString(1, KeyName.toUpperCase());
//			rs=sm.executeQuery();
//			if (rs.next())
//			{
//				Result=rs.getInt(1)+1;
//				sm2 = conn.prepareStatement("update ncounter set KeyCount=KeyCount+"+Integer.toString(Count)
//						+",EditWHo=?,EditDate=? where KeyName=?");
//				if (context!=null)
//				{
//					DBHelper.setValue(sm2, 1, UserID.toUpperCase());
//					DBHelper.setValue(sm2, 2, new Date(Calendar.getInstance().getTimeInMillis()));
//					DBHelper.setValue(sm2, 3, KeyName.toUpperCase());
//				}
//				else
//				{
//					sm2.setString(1, UserID.toUpperCase());
//					Calendar c = Calendar.getInstance();
//					c.set(Calendar.HOUR_OF_DAY, 11);
//					c.set(Calendar.MINUTE, 0);
//					c.set(Calendar.SECOND, 0);
//					c.set(Calendar.MILLISECOND, 0);
//					sm2.setDate(2, new java.sql.Date(c.getTimeInMillis()));
//					sm2.setString(3, KeyName.toUpperCase());
//
//				}
//				sm2.executeUpdate();
//			}
//			else
//			{
//				if (FirstValue<=0) throw new Exception("ncounter("+KeyName+") load error");
//				Result=FirstValue;
//				sm2 = conn.prepareStatement("insert into ncounter(KeyName,KeyCount,AddWHo,EditWHo) "
//						+" values('"+KeyName.toUpperCase()+"',"+Integer.toString(Count+FirstValue-1)+",'"+UserID+"','"+UserID+"')");
//				sm2.executeUpdate();
//			}
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(sm2);
//			DBHelper.release(rs,sm);
//		}
	}

//	public static int GetWarehouseNCounter(String warehouse,String KeyName,int Count,int FirstValue) throws Exception
//	{
//
//		PreparedStatement sm = null;
//		PreparedStatement sm2 = null;
//		ResultSet rs = null;
//		int Result=0;
//		try
//		{
//			if (Count<=0)  throw new Exception("Not find ("+KeyName+") by ncounter");
//			sm = conn.prepareStatement("Select KeyCount from "+warehouse+".ncounter where KeyName=?");
//			if (context!=null)
//				DBHelper.setValue(sm, 1, KeyName.toUpperCase());
//			else
//				sm.setString(1, KeyName.toUpperCase());
//			rs=sm.executeQuery();
//			if (rs.next())
//			{
//				Result=rs.getInt(1)+1;
//				sm2 = conn.prepareStatement("update "+warehouse+".ncounter set KeyCount=KeyCount+"+Integer.toString(Count)
//						+" where KeyName=?");
//				if (context!=null)
//					DBHelper.setValue(sm2, 1, KeyName.toUpperCase());
//				else
//					sm2.setString(1, KeyName.toUpperCase());
//				sm2.executeUpdate();
//			}
//			else
//			{
//				if (FirstValue<=0) throw new Exception("ncounter("+KeyName+") load error");
//				Result=FirstValue;
//				sm2 = conn.prepareStatement("insert into "+warehouse+".ncounter(KeyName,KeyCount) "
//						+" values('"+KeyName.toUpperCase()+"',"+Integer.toString(Count+FirstValue-1)+")");
//				sm2.executeUpdate();
//			}
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}
//		finally {
//			DBHelper.release(sm2);
//			DBHelper.release(rs,sm);
//		}
//	}

//
//	public static HashMap<String,String> GetValueMap(String Sql,String[] Params) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetValueMap--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------GetValueMap---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		HashMap<String,String> Result=new HashMap<String,String>();
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			rs=sm.executeQuery();
//			ResultSetMetaData data = rs.getMetaData();
//			if (rs.next())
//			{
//				for (int i = 1; i <= data.getColumnCount(); i++)
//				{
//					Result.put(data.getColumnName(i) , rs.getString(i));
//				}
//			}
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}
//		finally {
//			DBHelper.release(rs,sm);
//		}
//	}
//
//	public static ArrayList<String[]> GetRecordList(String Sql,String[] Params) throws Exception
//	{
//		return GetRecordList(Sql,Params,0);
//	}
//
//	public static ArrayList<String[]> GetRecordList(String Sql,String[] Params,int ReturnFieldCount) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetRecordList--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------GetRecordList---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		ArrayList<String[]> aResult=null;
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		String[] Result=null;
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			rs=sm.executeQuery();
//			while (rs.next())
//			{
//				if (aResult==null) aResult=new ArrayList<String[]>();
//				if (ReturnFieldCount==0) ReturnFieldCount=rs.getMetaData().getColumnCount();
//				Result=new String[ReturnFieldCount];
//				for(int i1=0;i1<ReturnFieldCount;i1++)
//					Result[i1]= rs.getString(i1+1);
//				aResult.add(Result);
//			}
//			return aResult;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(rs,sm);
//		}
//	}

//	public static String[] GetValueList(String Sql,String[] Params) throws Exception
//	{
//		return GetValueList(Sql,Params,0);
//	}
//
//	public static String[] GetValueList(String Sql,String[] Params,int ReturnFieldCount) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetValueList--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------GetValueList---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		String[] Result=null;
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			rs=sm.executeQuery();
//			if (rs.next())
//			{
//				if (ReturnFieldCount==0) ReturnFieldCount=rs.getMetaData().getColumnCount();
//				Result=new String[ReturnFieldCount];
//				for(int i1=0;i1<ReturnFieldCount;i1++)
//					Result[i1]= rs.getString(i1+1);
//			}
//			return Result;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(rs,sm);
//		}
//	}

//
//	public static List<HashMap<String,String>> GetRecordMap(String Sql,ArrayList<String> Params) throws Exception
//	{
//		String[] aParams=new String[Params.size()];
//		for(int i1=0;i1<Params.size();i1++) aParams[i1]=Params.get(i1);
//		return GetRecordMap(Sql,aParams);
//	}
//
//	public static List<HashMap<String,String>> GetRecordMap(String Sql,String[] Params) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--GetRecordMap--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------GetRecordMap---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//		List<HashMap<String,String>> aResult=null;
//		PreparedStatement sm = null;
//		ResultSet rs = null;
//		String[] ResultField=null;
//		try
//		{
//			sm = conn.prepareStatement(Sql);
//			for(int i1=0;i1<Params.length;i1++)
//				setParamValue(sm,i1+1,Params[i1]);
//			int ReturnFieldCount=0;
//			rs=sm.executeQuery();
//			while (rs.next())
//			{
//				if (aResult==null)
//				{
//					aResult=new List<HashMap<String,String>>();
//					ReturnFieldCount=rs.getMetaData().getColumnCount();
//					ResultField=new String[ReturnFieldCount];
//					for(int i1=0;i1<ReturnFieldCount;i1++)
//						ResultField[i1]=rs.getMetaData().getColumnName(i1+1);
//				}
//				HashMap<String,String> mRecord=new HashMap<String,String>();
//				for(int i1=0;i1<ReturnFieldCount;i1++)
//				{
//					mRecord.put(ResultField[i1], rs.getString(i1+1));
//				}
//				aResult.add(mRecord);
//			}
//			return aResult;
//		}
//		catch(Exception e0)
//		{
//			throw e0;
//		}finally {
//			DBHelper.release(rs,sm);
//		}
//	}

//
//	public void OpenSql(String Sql,ArrayList<String> Params) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.size(); i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params.get(i1) + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--OpenSql--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------OpenSql---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//
//		context=context;
//		theConnection=conn;
//		//sm = null;
//		//rs = null;
//		try
//		{
//			theStatement = DBHelper.executeUpdate(Sql);
//			if (Params!=null)
//			{
//				for(int i1=0;i1<Params.size();i1++)
//					setParamValue(theStatement,i1+1,Params.get(i1));
//			}
//			theResultSet=theStatement.executeQuery();
//			return;
//		}
//		catch(Exception e0)
//		{
//			DBHelper.release(theResultSet,theStatement,theConnection);
//			throw e0;
//		}
//	}
//
//	public void OpenSql(String Sql,String[] Params) throws Exception
//	{
//
//		if (LegacyDBHelper.Isdebug)
//		{
//			String SqlShow=Sql;
//			if(Params!=null) {
//				for (int i1 = 0; i1 < Params.length; i1++) {
//					int i2 = SqlShow.indexOf('?');
//					if (i2 >= 0)
//						SqlShow = SqlShow.substring(0, i2) + "'" + Params[i1] + "'" + SqlShow.substring(i2 + 1);
//				}
//			}
//			System.out.println("--OpenSql--------------------------------------");
//			System.out.println("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//
//			sqlLogger.info("-------------------OpenSql---------------------");
//			sqlLogger.info("--user id: "+EHContextHelper.getUser().getUsername()+" sql: "+SqlShow);
//		}
//
//
//		context=context;
//		theConnection=conn;
//		//sm = null;
//		//rs = null;
//		try
//		{
//			theStatement = DBHelper.executeUpdate(Sql);
//			if (Params!=null)
//			{
//				for(int i1=0;i1<Params.length;i1++)
//					setParamValue(theStatement,i1+1,Params[i1]);
//			}
//			theResultSet=theStatement.executeQuery();
//			return;
//		}
//		catch(Exception e0)
//		{
//			DBHelper.release(theResultSet,theStatement,theConnection);
//			throw e0;
//		}
//	}

//	public void Close()
//	{
//		DBHelper.release(theResultSet,theStatement,theConnection);
//	}

	public static boolean isDateParam(String Param)
	{
		if (Param==null) return false;
		if ((Param.length()>=5)&&(Param.substring(0,5).equals("@date")))
			return true;
		else
			return false;
	}
//
//	public static void setParamValue(PreparedStatement statement,int paramIdx,String param) throws SQLException, ParseException
//	{
//		if (isDateParam(param))
//		{
//			if ((param.equals("@date"))||(param.equals("@date11")))
//			{
//				if (context!=null)
//				{
//					Date d = new Date(Calendar.getInstance().getTimeInMillis());
//					if (param.equals("@date11"))
//					{
//						// Get the current date
//						Calendar calendar = Calendar.getInstance();
//						java.util.Date currentDate = calendar.getTime();
//
//						// Set the time to 11am
//						calendar.setTime(currentDate);
//						calendar.set(Calendar.HOUR_OF_DAY, 11);
//						calendar.set(Calendar.MINUTE, 0);
//						calendar.set(Calendar.SECOND, 0);
//
//						// Create a java.sql.Time object for 11am
//						Time time = new Time(calendar.getTimeInMillis());
//
//						// Create a java.sql.Date object for the current date
//						Date date = new Date(currentDate.getTime());
//
//						// Combine the date and time components
//						java.util.Date dateTime = new java.util.Date(date.getTime() + time.getTime());
//
//						// Create a java.sql.Date object with time at 11am
//						date = new Date(dateTime.getTime());
//					}
//					DBHelper.setValue(statement, paramIdx, d);
//				}
//				else
//				{
//					Calendar c = Calendar.getInstance();
//					if (param.equals("@date"))
//						c.add(Calendar.HOUR_OF_DAY, -8);
//					else
//					{
//						c.set(Calendar.HOUR_OF_DAY, 11);
//						c.set(Calendar.MINUTE, 0);
//						c.set(Calendar.SECOND, 0);
//						c.set(Calendar.MILLISECOND, 0);
//					}
//					statement.setDate(paramIdx, new java.sql.Date(c.getTimeInMillis()));
//				}
//			}
//			else
//			{
//				String s1=param.substring(6);
//				if (LegecyUtilHelper.isNull2(s1))
//				{
//						statement.setDate(paramIdx, null);
//				}
//				else
//				{
//					String Format=null;
//
//					if (s1.length()==6) Format="yyMMdd";
//					else if (s1.length()==8) Format="yyyyMMdd";
//					else {
//						if (s1.indexOf('-') > 0) Format = "yyyy-MM-dd";
//						else if (s1.indexOf('/') > 0) Format = "MM/dd/yyyy";
//						else Format = "yyyyMMdd";
//						if (s1.indexOf(':') > 0) Format += " HH:mm:ss";
//					}
//					SimpleDateFormat sdf = new SimpleDateFormat(Format);
//					java.util.Date d1=sdf.parse(s1);
//					if (context!=null)
//						DBHelper.setValue(statement, paramIdx, new java.sql.Date(d1.getTime()));
//					else
//						statement.setDate(paramIdx,  new java.sql.Date(d1.getTime()));
//				}
//			}
//		}
//		else
//		{
//			if (context!=null)
//				DBHelper.setValue(statement, paramIdx, LegecyUtilHelper.NzParams(param));
//			else
//				statement.setString(paramIdx, LegecyUtilHelper.NzParams(param));
//		}
//	}


}
