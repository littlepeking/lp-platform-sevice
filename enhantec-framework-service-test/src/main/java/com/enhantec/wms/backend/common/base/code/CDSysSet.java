package com.enhantec.wms.backend.common.base.code;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.framework.UserInfo;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CDSysSet {

    //复验期到期质量状态
    public static String getElot05ExpiredQualityStatus(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "ELOT05CONF");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF1")) ? codeHashMap.get("UDF1"): "QUARANTINE";

    }
    //有效期到期质量状态
    public static String getElot11ExpiredQualityStatus(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "ELOT11CONF");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF1")) ? codeHashMap.get("UDF1"): "QUARANTINE";

    }
    // 到期提醒的错误名称（默认为"复验期",CSS需配置为"停止发运期"）
    public static String getElot05DisplayName(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "ELOT05CONF");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF2")) ? codeHashMap.get("UDF2"): "复验";

    }

    public static int getElot05MaxRemindDays(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "ELOT05CONF");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF3")) ? Integer.parseInt(codeHashMap.get("UDF3")): 15;

    }

    public static int getElot05MaxRemindTimes(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "ELOT05CONF");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF4")) ? Integer.parseInt(codeHashMap.get("UDF4")): 999;

    }

    public static String getDefaultProjectCode(Context context, Connection connection){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "DEFPRJCODE");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF1")) ? codeHashMap.get("UDF1"): "COMMONPROJECT";

    }

    public static String getDefaultProjectCode(UserInfo userInfo){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(userInfo, "SYSSET", "DEFPRJCODE");

        return !UtilHelper.isEmpty(codeHashMap.get("UDF1")) ? codeHashMap.get("UDF1"): "COMMONPROJECT";

    }

    public static String getSampleOrderType(UserInfo userInfo){

        HashMap<String,String> codeHashMap = CodeLookup.getCodeLookupByKey(userInfo, "SYSSET", "SAMPLEORD");

        return codeHashMap.get("UDF1");

    }

    public static String getSampleOrderType(Context context, Connection connection){

        return CodeLookup.getCodeLookupValue(context, connection, "SYSSET", "SAMPLEORD","UDF1","默认取样出库单类型");

    }

    //留样入库默认入库类型
    public static String getSampleReceiptType(Context context, Connection conn) {

        return CodeLookup.getCodeLookupValue(context, conn, "SYSSET", "SAMPLEREC","UDF1","留样入库默认收货单类型配置");

    }

    public static String getPOReceiptType(Context context, Connection conn) {

        return CodeLookup.getCodeLookupValue(context, conn, "SYSSET", "PORECTYPE","UDF1","采购订单默认收货单类型配置");

    }

    public static String getStorerKey(Context context, Connection conn) {

        return String.valueOf(DBHelper.getValue(context, conn, "select UDF1 from Codelkup where ListName=? and Code=?",
                new Object[]{"SYSSET","STORERKEY"}, "默认货主"));

    }

    public static boolean isAllowMoveSN(Context context, Connection conn) {

        return "Y".equalsIgnoreCase(CodeLookup.getCodeLookupValue(context, conn, "SYSSET", "ALLOWMVSN","UDF1"," 是否允许RF直接移动唯一码"));

    }

    public static boolean mustProvideToIdIfSplitSN(Context context, Connection conn) {

        return "Y".equalsIgnoreCase(CodeLookup.getCodeLookupValue(context, conn, "SYSSET", "SPLITWTOID","UDF1","唯一码管理的物料是否必须提供分拆至箱号"));

    }

    /**
     * 唯一码管理的sku生成箱号的方式。
     * 1.手动生成箱号
     * 2.自动生成箱号
     * 3.不生成箱号（每个SN都使用独立的流水码箱号）
     */
    public static String getSNGenerateLpnType(Context context, Connection conn){
        return CodeLookup.getCodeLookupValue(context, conn, "SYSSET", "SNGENIDTYP","UDF1","唯一码管理的sku生成箱号的方式");

    }


    /**
     * 获取分装出库类型代码
     * @param context
     * @param connection
     * @return
     */
    public static String getRePackOrderType(Context context,Connection connection){
        return CodeLookup.getCodeLookupValue(context,connection,"SYSSET","REPACKORDT","UDF1","分装出库类型代码");
    }


    /**
     * 是否发运前必须进行确认
     * 0 发运前不需要确认（不需要确认就可以拣货）1 发运前必须确认（不需要确认就可以拣货）2 建单必须确认后才能分配或发放（发运前必须要确认）
     */
    public static boolean isShipByConfirm(Context context, Connection conn){

        String isShipByConfirm = CodeLookup.getSysConfig(context, conn, "ORDCONFTYP","0");

        return "1".equalsIgnoreCase(isShipByConfirm)||"2".equalsIgnoreCase(isShipByConfirm);
    }


    /**
     * 标签是否打印重量信息
     * 如果配置成N，则标签不打印重量信息
     * @return
     */
    public static boolean enableLabelWgt(Context context,Connection connection)throws Exception{
        return "Y".equalsIgnoreCase(CodeLookup.getSysConfig(context, connection, "LABELWGT","Y"));
    }

    public static boolean enableSNwgt(Context context,Connection connection){
        return "Y".equalsIgnoreCase(CodeLookup.getSysConfig(context, connection, "ENABLESNWGT","Y"));
    }
    /**
     * 获取箱号前缀配置：UDF1，箱号前缀
     * 生基箱号规则=箱号前缀+批次号+两位流水
     */
    public static HashMap<String,String> getBoxPrefix(Context context,Connection connection){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","BOXPREFIX");
    }

    public static HashMap<String,String> getBoxPrefix(Context context,Connection connection,String dbId){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","BOXPREFIX",dbId);
    }

    /**
     * 获取批次前缀配置
     * UDF1：批次前缀
     * UDF2：时间格式，如'yyyyMMdd'
     * UDF3：流水号长度
     */
    public static HashMap<String,String> getLotPrefix(Context context,Connection connection){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","WAREHOUSE");
    }

    public static HashMap<String,String> getLotPrefix(Context context,Connection connection,String dbId){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","WAREHOUSE",dbId);
    }

    /**
     * 获取LPN前缀配置：UDF1，箱号前缀
     * 生基LPN规则=LPN前缀+批次号+两位流水
     */
    public static HashMap<String,String> getLpnPrefix(Context context,Connection connection){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","LPNPREFIX");
    }

    public static HashMap<String,String> getLpnPrefix(Context context,Connection connection,String dbId){
        return CodeLookup.getCodeLookupByKey(context,connection,"SYSSET","LPNPREFIX",dbId);
    }

    /**
     * 获取不打印LPN标签的出库类型列表
     */
    public static List<String> getNotPrintLpnLabelOrderTypes(Context context,Connection connection){
        HashMap<String, String> notPrintConf = CodeLookup.getCodeLookupByKey(context, connection, "SYSSET", "NOTPRINT");
        if(UtilHelper.isEmpty(notPrintConf.get("UDF1"))){
            return null;
        }else{
            return Arrays.asList(notPrintConf.get("UDF1").split(","));
        }
    }

    /**
     * 唯一码标签是否显示重量
     * 唯一码重量发生变化时，是否打印标签
     */
    public static boolean snLabelWgt(Context context,Connection connection){
        return !"N".equalsIgnoreCase(CodeLookup.getCodeLookupValue(context,connection,"SYSSET","SNLABEL","UDF1",""));
    }



}
