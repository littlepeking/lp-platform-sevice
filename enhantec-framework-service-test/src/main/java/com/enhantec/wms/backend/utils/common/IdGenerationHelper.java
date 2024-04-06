package com.enhantec.wms.backend.utils.common;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.framework.common.utils.EHContextHelper;


public class IdGenerationHelper {
    /**
     * 检查容器条码规则或者箱号规则的配置是否准确
     * @return
     */
    public static boolean checkWareHouseOrBoxPrefixConf(Map<String, String> wareHouseOrBoxPrefixConfMap){
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

    public static String createReceiptLot( String sku) throws Exception
    {
        Map<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey("SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String skuTypeCode= DBHelper.getStringValue( "select udf4 from codelkup a,sku s where a.listname=? and a.code=s.busr4 and s.sku=?", new String[]{"SKUTYPE1",sku}, "");//根据sku获批号是sm||sc
        String CurDate= DBHelper.getStringValue( " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+skuTypeCode+CurDate;
        String num = String.valueOf(getNCounter( prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    public static String createReceiptLot() throws Exception
    {
        Map<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey("SYSSET","WAREHOUSE");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= DBHelper.getStringValue( " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter( prefix));

        while(num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;

        return  prefix + num;
    }

    /**
     * 根据批次号创建箱号
     * 生基箱号生成规则：箱号前缀+批次号+流水
     */
    public static String createBoxId(String receiptLot) throws Exception {
        receiptLot = CDSysSet.getBoxPrefix().get("UDF1") + receiptLot;
        String num = String.valueOf(getNCounter( receiptLot));
        while(num.length()<2) num="0"+num;
        return  receiptLot + num;
    }
    public static String createSNID() throws Exception
    {
        Map<String,String> lotRuleCodelkup= CodeLookup.getCodeLookupByKey("SYSSET","SNPREFIX");
        if(!checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.SNPREFIX配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String CurDate= DBHelper.getStringValue( " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+CurDate;
        String num = String.valueOf(getNCounter( prefix));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }
    /**
     * 生基LPN生成规则：LPN前缀+批次+流水号
     */
    public static String generateLpn( String prefix) throws Exception {
        prefix = CDSysSet.getLpnPrefix().get("UDF1") + prefix;
        String num = String.valueOf(getNCounter( prefix));
        while(num.length()<2) num="0"+num;
        return  prefix + num;
    }

    public static String generateID(String prefix, int sequenceLength)
    {
        String num = String.valueOf(getNCounter( prefix));

        while(num.length()<sequenceLength) num="0"+num;

        return  prefix + num;
    }

    public static String generateIDByKeyName(String keyName, int sequenceLength)
    {
        String num = String.valueOf(getNCounter( keyName));

        while(num.length()<sequenceLength) num="0"+num;

        return  num;
    }

    public static String createLpnOrBoxIdFromExistingLpn( String lpn) {
        //该LPN下，生成下一个分装桶的字母
        int alphabetOffset = getNCounter( lpn)-1;
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

    public static String createSubReceiptLot( String receiptLot, String lotTypePrefix)
    {
        if(UtilHelper.isEmpty(lotTypePrefix)) ExceptionHelper.throwRfFulfillLogicException("子批次前缀不能为空，否则会和容器条码的NCOUNTER使用的号段冲突");
        int alphabetOffset = getNCounter( receiptLot+lotTypePrefix)-1;

        //因每个桶分装后加一位英文字母作为分装子桶，需限制超出字母范围的情况
        if(alphabetOffset>26) ExceptionHelper.throwRfFulfillLogicException("子批次最多允许创建26次");
        char nextChar = (char) ('A'+ alphabetOffset);

        return  receiptLot + lotTypePrefix + nextChar;
    }

//    public static void createLpnFromExistingLpnRollback( String lpn) throws Exception {
//
//
//        PreparedStatement sm = null;
//
//        try {
//
//
//            sm = conn.prepareStatement("update ncounter set KeyCount=KeyCount-1"
//                    + ",EditWHo=?,EditDate=? where KeyName=?");
//            DBHelper.setValue(sm, 1, EHContextHelper.getUser().getUsername().toUpperCase());
//            DBHelper.setValue(sm, 2, new java.util.Date(Calendar.getInstance().getTimeInMillis()));
//            DBHelper.setValue(sm, 3, lpn);
//
//            sm.executeUpdate();
//
//        } finally {
//           DBHelper.release(sm);
//        }
//    }

    public static void resetNCounter( String prefix) throws Exception {

             DBHelper.executeUpdate( "update ncounter set KeyCount= 0"
                    + ",EditWHo=?,EditDate=? where KeyName=?",
                    new Object[]{EHContextHelper.getUser().getUsername().toUpperCase(),
                    new java.util.Date(Calendar.getInstance().getTimeInMillis())
                    , prefix});
    }


    /**
     * 生成的LPN箱号为 原LPN的箱号+至LPN的后缀
     */
    public static String getBarrelNumberFromLpn( String newLpn) {

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

        Map<String,String> parentIdnotes = IDNotes.findById(parentLpn,true);

        return parentIdnotes.get("BARRELNUMBER")+suffix;

    }

    public static int getEnterpriseNCounter( String UserID, String name, int LpnCount) throws Exception
    {
        String iCnt= DBHelper.getStringValue( "SELECT KEYCOUNT FROM ENTERPRISE.NCOUNTER WHERE KEYNAME=?", new String[]{name}, null);
        if (iCnt==null)
        {
            DBHelper.executeUpdate( "INSERT INTO ENTERPRISE.NCOUNTER(KEYNAME,KEYCOUNT,ADDWHO,EDITWHO) VALUES(?,?,?,?)"
                    , new String[]{name,Integer.toString(LpnCount),UserID,UserID});
            return 1;
        }
        else
        {
            DBHelper.executeUpdate( "UPDATE ENTERPRISE.NCOUNTER SET KEYCOUNT=KEYCOUNT+?,EDITWHO=?,EDITDATE=? WHERE KEYNAME=?"
                    , new String[]{Integer.toString(LpnCount),UserID, LocalDateTime.now().toString(),name});
            return Integer.parseInt(iCnt)+1;
        }
    }


    public static String fillStringWithZero(int ID, int len)
    {
        String s1=Integer.toString(ID);
        while (s1.length()<len) s1="0"+s1;
        return s1;
    }


    public static String getNCounterStrWithLength(String KeyName, int len)
    {
        return fillStringWithZero(getNCounter("System",KeyName,1,1),len);
    }

    public static String getNCounterStr(String KeyName)
    {
        return String.valueOf(getNCounter("System",KeyName,1,1));
    }


    public static int getNCounter(String KeyName)
    {
        return getNCounter("System",KeyName,1,1);
    }


    public static int getNCounter(String KeyName,int Count)
    {
        return getNCounter("System",KeyName,Count,1);
    }


    public static int getNCounter(String KeyName,int Count,int FirstValue)
    {
        return getNCounter("System",KeyName,Count,FirstValue);
    }

    public static int getNCounter(String userId,String KeyName,int Count,int FirstValue)
    {

            int result=0;

            if (Count<=0)  ExceptionHelper.throwRfFulfillLogicException("Not find ("+KeyName+") by ncounter");

            Map<String,Object> seqHashMap = DBHelper.getRawRecord("SELECT KEYCOUNT FROM NCOUNTER WHERE KEYNAME=?",new Object[]{KeyName.toUpperCase()},"", false);

            if (seqHashMap!=null)
            {
                result=(int) seqHashMap.get("KEYCOUNT")+1;
                DBHelper.executeUpdate("update ncounter set KeyCount=KeyCount+"+Integer.toString(Count)
                        +",EditWHo=?,EditDate=? where KeyName=?", new Object[]{userId.toUpperCase()
                        , UtilHelper.getCurrentSqlDate11(), KeyName.toUpperCase()});
            }
            else
            {
                if (FirstValue<=0) ExceptionHelper.throwRfFulfillLogicException("ncounter("+KeyName+") load error");
                result=FirstValue;
                DBHelper.executeUpdate("insert into ncounter(KeyName,KeyCount,AddWHo,EditWHo) "
                        +" values('"+KeyName.toUpperCase()+"',"+Integer.toString(Count+FirstValue-1)+",'"+userId+"','"+userId+"')", new Object[]{});
            }
            return result;

    }

}
