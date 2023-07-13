package com.enhantec.wms.backend.utils.common;

import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.KeyGen;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.Context;


public class IdGenerationHelper {
    /**
     * 检查容器条码规则或者箱号规则的配置是否准确
     * @return
     */
    public static boolean checkWareHouseOrBoxPrefixConf(HashMap<String, String> wareHouseOrBoxPrefixConfMap){
        if(UtilHelper.isEmpty(wareHouseOrBoxPrefixConfMap.get("UDF2")) && !isDateTimeConf(wareHouseOrBoxPrefixConfMap.get("UDF2"))){
            return false;
        }
        if(UtilHelper.isEmpty(wareHouseOrBoxPrefixConfMap.get("UDF3"))){
            return false;
        }else{
            for(int i=0;i<wareHouseOrBoxPrefixConfMap.get("UDF3").length();i++){
                if(!Character.isDigit(wareHouseOrBoxPrefixConfMap.get("UDF3").charAt(i)))
                    return false;
            }
        }
        return true;
    }

    /**
     * 判断是字符串否符合格式化字符串的格式的形式
     * @return
     */
    private static boolean isDateTimeConf(String dateTimeString){
        if(UtilHelper.isEmpty(dateTimeString) || dateTimeString.length() < 2) return false;
        if(!dateTimeString.matches("[YyMmdHhs]*")) return false;
        return true;
    }

    public static String createReceiptLot(Context context, Connection conn, String sku) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,conn,"SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String skuTypeCode= LegacyDBHelper.GetValue(context, conn, "select udf4 from codelkup a,sku s where a.listname=? and a.code=s.busr4 and s.sku=?", new String[]{"SKUTYPE1",sku}, "");//根据sku获批号是sm||sc
        String CurDate= LegacyDBHelper.GetValue(context, conn, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+skuTypeCode+CurDate;
        String num = String.valueOf(getNCounter(context, conn, prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    public static String createReceiptLot(Context context, Connection conn) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,conn,"SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= LegacyDBHelper.GetValue(context, conn, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter(context, conn, prefix));

        while(num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;

        return  prefix + num;
    }

    /**
     * 根据批次号创建箱号
     * 生基箱号生成规则：箱号前缀+批次号+流水
     */
    public static String createBoxId(Context context, Connection conn,String receiptLot) throws Exception {
        receiptLot = CDSysSet.getBoxPrefix(context,conn).get("UDF1") + receiptLot;
        String num = String.valueOf(getNCounter(context, conn, receiptLot));
        while(num.length()<2) num="0"+num;
        return  receiptLot + num;
    }
    public static String createSNID(Context context, Connection conn) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,conn,"SYSSET","SNPREFIX");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.SNPREFIX配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= LegacyDBHelper.GetValue(context, conn, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter(context, conn, prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    /**
     * 生基LPN生成规则：LPN前缀+批次+流水号
     */
    public static String generateLpn(Context context, Connection conn, String prefix) throws Exception {
        prefix = CDSysSet.getLpnPrefix(context,conn).get("UDF1") + prefix;
        String num = String.valueOf(getNCounter(context, conn, prefix));
        while(num.length()<2) num="0"+num;
        return  prefix + num;
    }

    public static String generateID(Context context, Connection conn, String UserID, String prefix, int sequenceLength) throws Exception
    {
        String num = String.valueOf(getNCounter(context, conn, prefix));

        while(num.length()<sequenceLength) num="0"+num;

        return  prefix + num;
    }

    public static String generateIDByKeyName(Context context, Connection conn, String UserID, String keyName, int sequenceLength) throws Exception
    {
        String num = String.valueOf(getNCounter(context, conn, keyName));

        while(num.length()<sequenceLength) num="0"+num;

        return  num;
    }

    public static String createLpnOrBoxIdFromExistingLpn(Context context, Connection conn, String lpn) throws Exception {
        //该LPN下，生成下一个分装桶的字母
        int alphabetOffset = getNCounter(context, conn, lpn)-1;
        if(alphabetOffset>1000) ExceptionHelper.throwRfFulfillLogicException("分装最多允许1000次");
        //因每个桶分装后加一位英文字母作为分装子桶，需限制超出字母范围的情况
        String nextString = "";

        if(alphabetOffset<26) {
            char nextChar = (char) ('A'+ alphabetOffset);
            nextString += nextChar;
        }else{
            String num = String.valueOf(alphabetOffset-25);
            while(num.length()<3) num="0"+num;
            nextString =  "C" + num;
        }

        return  lpn + nextString;
    }

    public static String createSubReceiptLot(Context context, Connection conn, String receiptLot, String lotTypePrefix) throws Exception
    {
        if(UtilHelper.isEmpty(lotTypePrefix)) ExceptionHelper.throwRfFulfillLogicException("子批次前缀不能为空，否则会和容器条码的NCOUNTER使用的号段冲突");
        int alphabetOffset = getNCounter(context, conn, receiptLot+lotTypePrefix)-1;

        //因每个桶分装后加一位英文字母作为分装子桶，需限制超出字母范围的情况
        if(alphabetOffset>26) ExceptionHelper.throwRfFulfillLogicException("子批次最多允许创建26次");
        char nextChar = (char) ('A'+ alphabetOffset);

        return  receiptLot + lotTypePrefix + nextChar;
    }

    public static void createLpnFromExistingLpnRollback(Context context, String lpn) throws Exception {

        Connection conn = null;
        PreparedStatement sm = null;

        try {

            conn = context.getConnection();
            sm = conn.prepareStatement("update ncounter set KeyCount=KeyCount-1"
                    + ",EditWHo=?,EditDate=? where KeyName=?");
            DBHelper.setValue(sm, 1, context.getUserID().toUpperCase());
            DBHelper.setValue(sm, 2, new java.util.Date(Calendar.getInstance().getTimeInMillis()));
            DBHelper.setValue(sm, 3, lpn);

            sm.executeUpdate();

        } finally {
           DBHelper.release(context, null,sm,conn);
        }
    }

    public static void resetNCounter(Context context, String prefix) throws Exception {

        Connection conn = null;
        PreparedStatement sm = null;

        try {

            conn = context.getConnection();
            sm = conn.prepareStatement("update ncounter set KeyCount= 0"
                    + ",EditWHo=?,EditDate=? where KeyName=?");
            DBHelper.setValue(sm, 1, context.getUserID().toUpperCase());
            DBHelper.setValue(sm, 2, new java.util.Date(Calendar.getInstance().getTimeInMillis()));
            DBHelper.setValue(sm, 3, prefix);

            sm.executeUpdate();

        } finally {
            context.releaseStatement(sm);
            context.releaseConnection(conn);
        }
    }


    /**
     * 生成的LPN箱号为 原LPN的箱号+至LPN的后缀
     */
    public static String getBarrelNumberFromLpn(Context context, Connection conn, String newLpn) {

//        int numberCharCount =0;
//        int barrelNumberStartIndex =-1;
//        for (int i = newLpn.length() - 1; i >= 0; i --) {//            char c = newLpn.charAt(i);
//            if (Character.isDigit(c)) numberCharCount++;
//            // BarrelNumber starts from the index of last 3rd number
//            if (numberCharCount == 3) {
//                barrelNumberStartIndex = i;
//                break;
//            }
//        }
//        String result = newLpn.substring(barrelNumberStartIndex);
//        return result;

        String parentLpn = newLpn.substring(0,newLpn.length()-1);
        String suffix = newLpn.substring(newLpn.length()-1);

        HashMap<String,String> parentIdnotes = IDNotes.findById(context,conn,parentLpn,true);

        return parentIdnotes.get("BARRELNUMBER")+suffix;

    }

    public static int getEnterpriseNCounter(Context context, Connection conn, String UserID, String name, int LpnCount) throws Exception
    {
        String iCnt= LegacyDBHelper.GetValue(context, conn
                , "SELECT KEYCOUNT FROM ENTERPRISE.NCOUNTER WHERE KEYNAME=?", new String[]{name}, null);
        if (iCnt==null)
        {
            LegacyDBHelper.ExecSql(context, conn
                    , "INSERT INTO ENTERPRISE.NCOUNTER(KEYNAME,KEYCOUNT,ADDWHO,EDITWHO) VALUES(?,?,?,?)"
                    , new String[]{name,Integer.toString(LpnCount),UserID,UserID});
            return 1;
        }
        else
        {
            LegacyDBHelper.ExecSql(context, conn
                    , "UPDATE ENTERPRISE.NCOUNTER SET KEYCOUNT=KEYCOUNT+?,EDITWHO=?,EDITDATE=? WHERE KEYNAME=?"
                    , new String[]{Integer.toString(LpnCount),UserID,"@date",name});
            return Integer.parseInt(iCnt)+1;
        }
    }



    public static int getNCounter(Context context,Connection conn,String KeyName) throws SQLException
    {
        return getNCounter(context,conn,"System",KeyName,1,1);
    }


    public static int getNCounter(Context context,Connection conn,String KeyName,int Count) throws SQLException
    {
        return getNCounter(context,conn,"System",KeyName,Count,1);
    }


    public static int getNCounter(Context context,Connection conn,String KeyName,int Count,int FirstValue) throws SQLException
    {
        return getNCounter(context,conn,"System",KeyName,Count,FirstValue);
    }

    public static int getNCounter(Context context,Connection conn,String UserID,String KeyName,int Count,int FirstValue) throws SQLException
    {

        PreparedStatement sm = null;
        PreparedStatement sm2 = null;
        ResultSet rs = null;
        int Result=0;
        try
        {
            if (Count<=0)  ExceptionHelper.throwRfFulfillLogicException("Not find ("+KeyName+") by ncounter");
            if(conn == null || conn.isClosed()) conn = context.getConnection();
            sm = conn.prepareStatement("Select KeyCount from ncounter where KeyName=?");
            if (context!=null)
                DBHelper.setValue(sm, 1, KeyName.toUpperCase());
            else
                sm.setString(1, KeyName.toUpperCase());
            rs=sm.executeQuery();
            if (rs.next())
            {
                Result=rs.getInt(1)+1;
                sm2 = conn.prepareStatement("update ncounter set KeyCount=KeyCount+"+Integer.toString(Count)
                        +",EditWHo=?,EditDate=? where KeyName=?");
                if (context!=null)
                {
                    DBHelper.setValue(sm2, 1, UserID.toUpperCase());
                    DBHelper.setValue(sm2, 2, new java.util.Date(Calendar.getInstance().getTimeInMillis()));
                    DBHelper.setValue(sm2, 3, KeyName.toUpperCase());
                }
                else
                {
                    sm2.setString(1, UserID.toUpperCase());
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, 11);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    sm2.setDate(2, new java.sql.Date(c.getTimeInMillis()));
                    sm2.setString(3, KeyName.toUpperCase());
                }
                sm2.executeUpdate();
            }
            else
            {
                if (FirstValue<=0) ExceptionHelper.throwRfFulfillLogicException("ncounter("+KeyName+") load error");
                Result=FirstValue;
                sm2 = conn.prepareStatement("insert into ncounter(KeyName,KeyCount,AddWHo,EditWHo) "
                        +" values('"+KeyName.toUpperCase()+"',"+Integer.toString(Count+FirstValue-1)+",'"+UserID+"','"+UserID+"')");
                sm2.executeUpdate();
            }
            return Result;
        }
        finally
        {
            try	{ if (rs!=null) context.releaseResultSet(rs); } catch(Exception e1){
                try{ rs.close();}  catch(Exception e2){}
            }

            try	{ if (sm!=null)  context.releaseStatement(sm);  } catch(Exception e1){
                try{ sm.close();}  catch(Exception e2){}
            }
            try	{ if (sm2!=null)  context.releaseStatement(sm2);  } catch(Exception e1){
                try{ sm2.close();}  catch(Exception e2){}
            }

            try{context.releaseConnection(conn);}  catch(Exception e2){}
        }
    }

    public static String getNextKey(Context context,String keyName)
    {
        return KeyGen.getKey(context, keyName, 2, 10);
    }

}
