package com.enhantec.wms.backend.utils.common;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.enhantec.framework.common.exception.EHApplicationException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class UtilHelper {

    public static boolean isEmpty(Object obj) {
        return obj==null || isNull(obj.toString());
    }


    public static boolean isEmpty(String str) {
        return (str == null) || str.trim().isEmpty() || isNull(str);
    }

    public static boolean isEmpty(String... strs){
        for (String str : strs) {
            if(isEmpty(str)){
                return true;
            }
        }
        return false;
    }

    public static boolean hasIntersection(List list1, List list2) {

        List commonElements = new ArrayList<>(list1);

        commonElements.retainAll(list2);

        return !commonElements.isEmpty();
    }


    public static boolean isNull(String vValue)
    {
        if (vValue==null) return true;
        if (vValue.equals("null")) return true;
        if (vValue.equals("NULL")) return true;
        if (vValue.equals("<EMPTY>")) return true;
        if (vValue.trim().equals("")) return true;
        return false;
    }

    public static String intStrAdd(String num1, String num2) {
        Integer num1Val;
        Integer num2Val;

        if(UtilHelper.isEmpty(num1)) num1 ="0";
        if(UtilHelper.isEmpty(num2)) num2 ="0";

        try {
            num1Val = new Integer(num1);
            num2Val = new Integer(num2);

        } catch (Exception e) {
            throw new RuntimeException("输入的数字非法");
        }

        return String.valueOf(num1Val+num2Val);

    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(UtilHelper.isEmpty(s)) return false;

        return str2Decimal(s,"字符串",false).remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    public static String nvl(String str, String defaultValue){
        return isNull(str) ? defaultValue: str;
    }

    public static boolean equals(String str1, String str2) {
        if (isEmpty(str1) && isEmpty(str2)) return true;
        if (isEmpty(str1) || isEmpty(str2)) return false;
        return str1.trim().equals(str2.trim());
    }

    public static String To_Char(int ID, int len)
    {
        String s1=Integer.toString(ID);
        while (s1.length()<len) s1="0"+s1;
        return s1;
    }

    public static int decimalCompare(BigDecimal num1, BigDecimal num2) {

        if( num1 == null || num2 == null ) throw new EHApplicationException("待比较的两个数字均不能为空");

        return num1.compareTo(num2);

    }

    public static int decimalStrCompare(String num1, String num2){
        BigDecimal num1Val;
        BigDecimal num2Val;

        try {
            num1Val = new BigDecimal(num1);
            num2Val = new BigDecimal(num2);

        } catch (Exception e) {
            throw new RuntimeException("输入的数字非法");
        }

        return num1Val.compareTo(num2Val);

    }

    public static String decimalStrAdd(String num1, String num2) {
        BigDecimal num1Val;
        BigDecimal num2Val;

        try {
            num1Val = new BigDecimal(num1);
            num2Val = new BigDecimal(num2);

        } catch (Exception e) {
            throw new RuntimeException("输入的数字非法");
        }

        return trimZerosAndToStr(num1Val.add(num2Val).toPlainString());

    }

    public static String decimalStrAdd(String num1,BigDecimal num2){
        BigDecimal number1;
        try{
            number1 = new BigDecimal(num1);
        }catch (Exception e){
            throw new RuntimeException("输入的数字非法");
        }
        return trimZerosAndToStr(number1.add(num2).toPlainString());
    }

    public static String decimalStrSubtract(String num1, String num2) {
        BigDecimal num1Val;
        BigDecimal num2Val;

        try {
            num1Val = new BigDecimal(num1);
            num2Val = new BigDecimal(num2);

        } catch (Exception e) {
            throw new RuntimeException("输入的数字非法");
        }

        return trimZerosAndToStr(num1Val.subtract(num2Val).toPlainString());

    }

    public static String trimZerosAndToStr(String str) throws FulfillLogicException {

        return trimZerosAndToStr(new BigDecimal(str));
    }

    public static String trimZerosAndToStr(BigDecimal num) throws FulfillLogicException {

        try {
            if (num == null) return null;

            // bug fix for num.stripTrailingZeros() before jdk 1.8
            BigDecimal zero = BigDecimal.ZERO;
            if (num.compareTo(zero) == 0) {
                return "0";
            } else {
                return num.stripTrailingZeros().toPlainString();
            }


        } catch (Exception e) {
            ExceptionHelper.throwRfFulfillLogicException("输入的数字非法");
        }

        return null;
    }

    public static String trim(String str) {
        if (str == null) return null;
        else return str.trim();
    }

    public static String getString(Object obj) {
        if (obj == null) return "";
        else return obj.toString();
    }


    public static String getString(Object obj, String defaultStr) {
        if (obj == null || StringUtils.isEmpty(obj.toString()))
            return defaultStr;
        else
            return obj.toString();
    }


//    public static List<Map<String, String>> convertEXEDataObject2List(EXEDataObject res) {
//
//        List<Map<String, String>> list = new ArrayList<>();
//
//        if (res != null && res.getRowCount() != 0) {
//            res.setRow(1);
//            while (true) {
//                Map<String, String> tempObj = new HashMap<>();
//                for (Object columnName : res.getAttribNames()) {
//                    String col = ((TextData) columnName).getValue();
//                    String val = res.getString(col);
//                    tempObj.put(col, val);
//                }
//                list.add(tempObj);
//                if (!res.getNextRow()) {
//                    break;
//                }
//            }
//        }
//        return list;
//    }


    public static BigDecimal str2Decimal(Object num, String errMsgPrefix, boolean allowEmpty) {

        if ((num == null || UtilHelper.isEmpty(num.toString())) && !allowEmpty) {
            ExceptionHelper.throwRfFulfillLogicException(errMsgPrefix + "不允许为空");
            return null;
        } else {

            try {
                return new BigDecimal(num.toString());
            } catch (Exception e) {
                ExceptionHelper.throwRfFulfillLogicException(errMsgPrefix + " 为非法数字");
            }
        }

        return null;
    }

    public static String addPrefixZeros4Number(int num,int length){

        String numStr = num+"";
         while(numStr.length()<length)numStr="0"+numStr;

         return numStr;
    }
    /**
     * @Author: Allan
     * @Date: 2021/6/16
     * @Description: 根据dateString日期类型进行判断pattern是否合规（可为空）
     */
    public static void toDate(String dateString, String pattern, boolean allowNull,String errorName){
        if (allowNull&&UtilHelper.isEmpty(dateString)){
            return;
        }else if (!allowNull&&UtilHelper.isEmpty(dateString)) {
            throw new NullPointerException("日期为空并不允许为空");
        }else {
            toDate(dateString,pattern,errorName);
        }

    }
    /**
     * @Author: Allan
     * @Date: 2021/6/16
     * @Description: 根据dateString日期类型进行判断pattern是否合规
     */
    public static void toDate(String dateString, String pattern,String errorName){
        if(!UtilHelper.isEmpty(dateString)) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                Date date = simpleDateFormat.parse(dateString);
                isTrueDate(dateString,date,simpleDateFormat);
            } catch (Exception e) {
                ExceptionHelper.throwRfFulfillLogicException(errorName);
            }
        }
    }

    /**
     * @Author: Allan
     * @Date: 2021/8/23
     * @Description: 验证是否正确日期
     */
    public static void isTrueDate(String dateString,Date date,SimpleDateFormat simpleDateFormat) throws Exception {
        String DateToNewstr=simpleDateFormat.format(date);
        if (dateString.equals(DateToNewstr)){
            return;
        }else {
            throw new Exception();
        }
    }

    public static java.sql.Date getCurrentSqlDate(){

        return new java.sql.Date(Calendar.getInstance().getTimeInMillis());

    }

    public static java.sql.Date getCurrentSqlDate11(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 11);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);


       return new java.sql.Date(c.getTimeInMillis());
    }



    public static Date convertStringToSqlDate(String timestamp) {
        return convertTimestampToSqlDate(new Timestamp(Long.valueOf(timestamp)));
    }


    public static Date convertTimestampToSqlDate(Timestamp timestamp) {
        if (timestamp != null) {
            return new Date(timestamp.getTime());
        } else {
            return null;
        }
    }

    public static Map<String,String> jsonToMap(String t) throws JSONException {

        Map<String, String> map = new HashMap<>();
        JSONObject mapObj = JSONObject.parseObject(t);
        return prepareProperties(mapObj);
    }

    static private Map<String, String> prepareProperties(JSONObject jo) {
        Map<String, String> properties = new HashMap<>();
        if(jo == null) {
            return properties;
        }
        for(Object o: jo.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            Object key = e.getKey();
            Object value = e.getValue();
            properties.put(key.toString(), value==null? null : value.toString());
        }
        return properties;
    }

}
