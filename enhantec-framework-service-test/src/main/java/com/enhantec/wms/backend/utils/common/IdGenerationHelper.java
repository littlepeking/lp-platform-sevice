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

    public static String createReceiptLot(Context context, String sku) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,"SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String skuTypeCode= DBHelper.getValue(context, "select udf4 from codelkup a,sku s where a.listname=? and a.code=s.busr4 and s.sku=?", new String[]{"SKUTYPE1",sku}, "");//根据sku获批号是sm||sc
        String CurDate= DBHelper.getValue(context, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+skuTypeCode+CurDate;
        String num = String.valueOf(getNCounter(context, prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    public static String createReceiptLot(Context context) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,"SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= DBHelper.getValue(context, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter(context, prefix));

        while(num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;

        return  prefix + num;
    }

    /**
     * 根据批次号创建箱号
     * 生基箱号生成规则：箱号前缀+批次号+流水
     */
    public static String createBoxId(Context context,String receiptLot) throws Exception {
        receiptLot = CDSysSet.getBoxPrefix(context).get("UDF1") + receiptLot;
        String num = String.valueOf(getNCounter(context, receiptLot));
        while(num.length()<2) num="0"+num;
        return  receiptLot + num;
    }
    public static String createSNID(Context context) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey(context,"SYSSET","SNPREFIX");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.SNPREFIX配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= DBHelper.getValue(context, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter(context, prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    /**
     * 生基LPN生成规则：LPN前缀+批次+流水号
     */
    public static String generateLpn(Context context, String prefix) throws Exception {
        prefix = CDSysSet.getLpnPrefix(context).get("UDF1") + prefix;
        String num = String.valueOf(getNCounter(context, prefix));
        while(num.length()<2) num="0"+num;
        return  prefix + num;
    }

    public static String generateID(Context context, String UserID, String prefix, int sequenceLength) throws Exception
    {
        String num = String.valueOf(getNCounter(context, prefix));

        while(num.length()<sequenceLength) num="0"+num;

        return  prefix + num;
    }

    public static String generateIDByKeyName(Context context, String UserID, String keyName, int sequenceLength) throws Exception
    {
        String num = String.valueOf(getNCounter(context, keyName));

        while(num.length()<sequenceLength) num="0"+num;

        return  num;
    }

    public static String createLpnOrBoxIdFromExistingLpn(Context context, String lpn) throws Exception {
        //该LPN下，生成下一个分装桶的字母
        int alphabetOffset = getNCounter(context, lpn)-1;
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

    public static String createSubReceiptLot(Context context, String receiptLot, String lotTypePrefix) throws Exception
    {
        if(UtilHelper.isEmpty(lotTypePrefix)) ExceptionHelper.throwRfFulfillLogicException("子批次前缀不能为空，否则会和容器条码的NCOUNTER使用的号段冲突");
        int alphabetOffset = getNCounter(context, receiptLot+lotTypePrefix)-1;

        //因每个桶分装后加一位英文字母作为分装子桶，需限制超出字母范围的情况
        if(alphabetOffset>26) ExceptionHelper.throwRfFulfillLogicException("子批次最多允许创建26次");
        char nextChar = (char) ('A'+ alphabetOffset);

        return  receiptLot + lotTypePrefix + nextChar;
    }

//    public static void createLpnFromExistingLpnRollback(Context context, String lpn) throws Exception {
//
//
//        PreparedStatement sm = null;
//
//        try {
//
//
//            sm = conn.prepareStatement("update ncounter set KeyCount=KeyCount-1"
//                    + ",EditWHo=?,EditDate=? where KeyName=?");
//            DBHelper.setValue(sm, 1, context.getUserID().toUpperCase());
//            DBHelper.setValue(sm, 2, new java.util.Date(Calendar.getInstance().getTimeInMillis()));
//            DBHelper.setValue(sm, 3, lpn);
//
//            sm.executeUpdate();
//
//        } finally {
//           DBHelper.release(context,sm);
//        }
//    }

    public static void resetNCounter(Context context, String prefix) throws Exception {

             DBHelper.executeUpdate(context, "update ncounter set KeyCount= 0"
                    + ",EditWHo=?,EditDate=? where KeyName=?",
                    new Object[]{context.getUserID().toUpperCase(),
                    new java.util.Date(Calendar.getInstance().getTimeInMillis())
                    , prefix});
    }


    /**
     * 生成的LPN箱号为 原LPN的箱号+至LPN的后缀
     */
    public static String getBarrelNumberFromLpn(Context context, String newLpn) {

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

        HashMap<String,String> parentIdnotes = IDNotes.findById(context,parentLpn,true);

        return parentIdnotes.get("BARRELNUMBER")+suffix;

    }

    public static int getEnterpriseNCounter(Context context, String UserID, String name, int LpnCount) throws Exception
    {
        String iCnt= DBHelper.getValue(context                , "SELECT KEYCOUNT FROM ENTERPRISE.NCOUNTER WHERE KEYNAME=?", new String[]{name}, null);
        if (iCnt==null)
        {
            DBHelper.executeUpdate(context, "INSERT INTO ENTERPRISE.NCOUNTER(KEYNAME,KEYCOUNT,ADDWHO,EDITWHO) VALUES(?,?,?,?)"
                    , new String[]{name,Integer.toString(LpnCount),UserID,UserID});
            return 1;
        }
        else
        {
            DBHelper.executeUpdate(context, "UPDATE ENTERPRISE.NCOUNTER SET KEYCOUNT=KEYCOUNT+?,EDITWHO=?,EDITDATE=? WHERE KEYNAME=?"
                    , new String[]{Integer.toString(LpnCount),UserID,"@date",name});
            return Integer.parseInt(iCnt)+1;
        }
    }



    public static int getNCounter(Context context,String KeyName) throws SQLException
    {
        return getNCounter(context,"System",KeyName,1,1);
    }


    public static int getNCounter(Context context,String KeyName,int Count) throws SQLException
    {
        return getNCounter(context,"System",KeyName,Count,1);
    }


    public static int getNCounter(Context context,String KeyName,int Count,int FirstValue) throws SQLException
    {
        return getNCounter(context,"System",KeyName,Count,FirstValue);
    }

    public static int getNCounter(Context context,String UserID,String KeyName,int Count,int FirstValue) throws SQLException
    {

            int result=0;

            if (Count<=0)  ExceptionHelper.throwRfFulfillLogicException("Not find ("+KeyName+") by ncounter");

            HashMap<String,Object> seqHashMap = DBHelper.getRawRecord(context,"Select KeyCount from ncounter where KeyName=?",new Object[]{KeyName.toUpperCase()},"", false);

            if (seqHashMap!=null)
            {
                result=(int) seqHashMap.get("KeyCount")+1;
                DBHelper.executeUpdate(context,"update ncounter set KeyCount=KeyCount+"+Integer.toString(Count)
                        +",EditWHo=?,EditDate=? where KeyName=?", new Object[]{UserID.toUpperCase()
                        , UtilHelper.getCurrentSqlDate11(), KeyName.toUpperCase()});
            }
            else
            {
                if (FirstValue<=0) ExceptionHelper.throwRfFulfillLogicException("ncounter("+KeyName+") load error");
                result=FirstValue;
                DBHelper.executeUpdate(context,"insert into ncounter(KeyName,KeyCount,AddWHo,EditWHo) "
                        +" values('"+KeyName.toUpperCase()+"',"+Integer.toString(Count+FirstValue-1)+",'"+UserID+"','"+UserID+"')", new Object[]{});
            }
            return result;

    }

    public static String getNextKey(Context context,String keyName)
    {
        return KeyGen.getKey(context, keyName, 2, 10);
    }

}
