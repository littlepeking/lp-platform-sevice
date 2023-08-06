package com.enhantec.wms.backend.utils.common;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Deprecated
public class LegecyUtilHelper {
	
	
	public final static char SqlSkipField=11;
	public final static char SqlSkipRecord=12;
	
	

//	public static Map<String,String> FindMap(String Key,String Value,List<Map<String,String>> aList)
//	{
//		for(Map<String,String> mList:aList)
//		{
//			if (LegecyUtilHelper.Nz(mList.get(Key),"").equals(LegecyUtilHelper.Nz(Value, ""))) return mList;
//		}
//		return null;
//	}
	
	public static String FormatUdf(String vCaption,String vValue,int Length) throws Exception
	{
		if (LegecyUtilHelper.isNull(vValue)) return " ";
		String Result=vCaption+":"+vValue;
		if (Result.length()>Length) Result=Result.substring(0,Length);
		return Result;
	}
	
	public static String FindUdf(String vText,String vKey) throws Exception
	{
		if (vText==null) return null;
		if (vText.length()<vKey.length()+1) return null;
		if (vText.substring(0,vKey.length()+1).equals(vKey+":"))
			return vText.substring(vKey.length()+1);
		return null;
	}
	
	
	public static String FindUdf(Map<String,String> mData,String vKey) throws Exception
	{
		if (mData==null) return null;
		if (mData.isEmpty()) return null;
		for(int i1=1;i1<=20;i1++)
		{
			String vText=mData.get("SUSR"+Integer.toString(i1));
			if (vText!=null)
				if (vText.length()>vKey.length()+1)
					if (vText.substring(0,vKey.length()+1).equals(vKey+":"))
						return vText.substring(vKey.length()+1);
			
		}
		return null;
	}

	public static String FormatDateString(String vDate) throws Exception
	{//转换完格式 2020-04-15    如果带时间，则  2020-04-15 22:37:25
		//15-4月 -20
		//2020-04-15
		//04/15/2020

		if (vDate.indexOf("月")>=0)
		{//15-4月 -20    15-4月 -20 15:30:44
			String TimeString="";
			String[] aDate=vDate.split("-");
			if(aDate.length<3) throw new Exception(vDate+"不是合法日期格式");
			int iDD=Integer.parseInt(aDate[0]);
			if (iDD<1) throw new Exception(vDate+"不是合法日期格式");
			if (iDD>31) throw new Exception(vDate+"不是合法日期格式");
			String sDD=Integer.toString(iDD);
			if (sDD.length()==1) sDD="0"+sDD;
			//----------------------------------------------------------
			int iM=aDate[1].indexOf("月");
			if (iM<1) throw new Exception(vDate+"不是合法日期格式");
			String cMM=aDate[1].substring(0,iM);
			int iMM=Integer.parseInt(cMM);
			if (iMM<1) throw new Exception(vDate+"不是合法日期格式");
			if (iMM>12) throw new Exception(vDate+"不是合法日期格式");
			String sMM=Integer.toString(iMM);
			if (sMM.length()==1) sMM="0"+sMM;
			//----------------------------------------------------------
			String sYear=aDate[2];
			if (sYear.length()>4)
			{
				int iTime=sYear.indexOf(' ');
				if (iTime<1) throw new Exception(vDate+"不是合法日期格式");
				TimeString=sYear.substring(iTime+1);
				sYear=sYear.substring(0,iTime);
			}
			int iYY=Integer.parseInt(sYear);
			String sYY="";
			if (iYY>1900)
			{
				if (iYY>=2100)  throw new Exception(vDate+"不是合法日期格式");
				sYY=Integer.toString(iYY);
			}
			else
			{
				if (iYY>=100) throw new Exception(vDate+"不是合法日期格式");
				sYY=Integer.toString(2000+iYY);
			}
			//------------------------------------------
			return sYY+"-"+sMM+"-"+sDD;
		}
		if (vDate.indexOf('T')>=0)
		{
			int i1=vDate.indexOf('T');
			return vDate.substring(0,i1);
		}
		return vDate;
	}

	public static String CheckBigDecimal(String Value,String ErrMsg) throws Exception
	{
		try {
			return  new BigDecimal(Value).setScale(5, BigDecimal.ROUND_UP).toString();
		}catch (Exception e){
			throw new Exception(ErrMsg);
		}
	}

	
	public static void CheckStringLength(String Value,int StrLong,String ErrMsg) throws Exception
	{
		if (!LegecyUtilHelper.isNull2(Value)){
			if (Value.length()>StrLong) throw new Exception(ErrMsg);
		}
	}


	public static void PutByComp(Map<String,String> map, String Key, String PutStr, String CompStr)
	{
		if (LegecyUtilHelper.isNull2(CompStr)){
			map.put(Key, PutStr);
		} else {
			if (!PutStr.equals(CompStr)){
				map.put(Key, PutStr);
			}
		}
	}

	public static String CheckStringNull(String Value,boolean CanNull,String DefaultValue,String ErrMsg) throws Exception
	{

		if (LegecyUtilHelper.isNull(Value))
		{
			if (!CanNull) throw new Exception(ErrMsg);
			return DefaultValue;
		}
		else
			return Value;
	}

	
	public static String IfValue(boolean bool, String TrueValue, String FalseValue) throws Exception
	{
		if (bool) 
			return TrueValue;
		else
			return FalseValue;
	}
	
	public static String NextChar(String vChar) throws Exception
	{
		if (vChar==null) throw new Exception("不能转换空值");
		if (vChar.equals("")) throw new Exception("不能转换空字符串");
		if (vChar.length()>2) throw new Exception("不能转换2位以上的串");
		if (vChar.equals(" ")) return "A";
		if (vChar.equals("Z")) return "AA";
		if (vChar.length()==1)
		{
			char a1=vChar.charAt(0);
			if ((a1<'A')||(a1>'Z')) throw new Exception("只能转换(A-Z)");
			a1++;
			return Character.toString(a1);
		}
		if (vChar.length()==2)
		{
			char a1=vChar.charAt(0);
			char a2=vChar.charAt(1);
			if ((a1<'A')||(a1>'Z')) throw new Exception("只能转换(A-Z)");
			if ((a2<'A')||(a2>'Z')) throw new Exception("只能转换(A-Z)");
			if (a2=='Z')
			{
				a1++;
				a2='A';
			}
			else
			{
				a2++;
			}
			return Character.toString(a1)+Character.toString(a2);
		}
	
		throw new Exception("原序号("+vChar+")有异常");
	}
	
	public static String toStringByCn(boolean isChinese, String CnStr,String EnStr)
	{
		if (isChinese)
			return CnStr;
		else
			return EnStr;
	}
	
    public static String To_Char(int ID, int len)
    {
        String s1=Integer.toString(ID);
        while (s1.length()<len) s1="0"+s1;
        return s1;
    }
    

	public static boolean CheckYesNo(String pValue)
	{
        if (pValue == null) return false;
        if (pValue.equals("1")) return true;
        if (pValue.equals("Y")) return true;
        if (pValue.equals("y")) return true;
        if (pValue.equals("YES")) return true;
        if (pValue.equals("yes")) return true;
        if (pValue.equals("Yes")) return true;
        if (pValue.equals("TRUE")) return true;
        if (pValue.equals("true")) return true;
        if (pValue.equals("True")) return true;
        if (pValue.equals("T")) return true;
        if (pValue.equals("t")) return true;
        if (pValue.equals("是")) return true;
        if (pValue.equals("对")) return true;
        return false;
	}
	
	
	public static String NextLineNumber(String pValue)
	{
		int iLine=Integer.parseInt(pValue);
		iLine++;
		String LineNumber=Integer.toString(iLine);
		while(LineNumber.length()<5) LineNumber="0"+LineNumber;
        return LineNumber;
	}
	
	
	public static String NzParams(String vValue)
	{
		//		/empty
		if (vValue==null) return vValue;
		if (vValue.equals("")) return null;  //非SQL型参数,如为空字符串,则转化成NULL,防止ORACLE不识别
		if (vValue.toUpperCase().equals("<NULL>")) return null;
		if (vValue.toUpperCase().equals("NULL")) return null;
		if (vValue.toUpperCase().equals("<EMPTY>")) return "";
		return vValue;
	}	
	
	
	
	public static boolean isNull(String vValue)
	{
		if (vValue==null) return true;
		if (vValue.equals("N/A")) return true;
		if (vValue.equals("null")) return true;
		if (vValue.equals("NULL")) return true;
		if (vValue.equals("<EMPTY>")) return true;
		if (vValue.trim().equals("")) return true;
		return false;
	}	
	
	public static boolean isNull(Object vValue)
	{
		if (vValue==null) return true;
		return isNull(vValue.toString());
	}
	
	public static boolean isNull2(String vValue)
	{
		if (vValue==null) return true;
		if (vValue.equals("N/A")) return true;
		if (vValue.equals("null")) return true;
		if (vValue.equals("NULL")) return true;
		if (vValue.equals("")) return true;
		return false;
	}
	
	public static String Nz(String vValue,String vDef)
	{
		if (isNull(vValue)) return vDef;
		return vValue;
	}	
	
	public static String Nz(Object vValue,String vDef)
	{
		if (vValue==null) return vDef;
		return Nz(vValue.toString(),vDef);
	}	
	
	public static String to_date(String vDate)
	{
		if (isNull(vDate))
			return null;
		else
		{
			char c1=vDate.charAt(4);
			if ((c1>='A'&&c1<='Z')||(c1>='a'&&c1<='z'))
			{
				return "to_date('"+vDate+"','DD-mon-YYYY HH24:MI:SS')";
			}
			if (vDate.substring(4,5).equals("-"))
			{
				return "to_date('"+vDate+"','YYYY-MM-DD HH24:MI:SS')";
			}
			else
			{
				return "to_date('"+vDate+"')";
			}
		}
	}
	
	public static boolean InArray(String CurValue,String Values)
	{
		String[] aValues=Values.split(",");
		for(int i1=0;i1<aValues.length;i1++)
		{
			if (CurValue.equals(aValues[i1])) return true; 
			if (aValues[i1].equals("*"))  return true; 
		}
		return false;
	}	
	
	public static boolean InArray(String CurValue,String[] Values)
	{
		for(int i1=0;i1<Values.length;i1++)
		{
			if (CurValue.equals(Values[i1])) return true; 
			if (Values[i1].equals("*"))  return true; 
		}
		return false;
	}
	
	
}
