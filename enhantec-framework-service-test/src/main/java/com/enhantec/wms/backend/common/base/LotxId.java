package com.enhantec.wms.backend.common.base;


import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.KeyGen;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class LotxId {

    public static HashMap<String,String> findDetailByReceiptLineAndSn(Context context, Connection connection, String receiptKey, String receiptLineNumber, String serialNumber, boolean checkExist)throws FulfillLogicException {
        if(UtilHelper.isEmpty(receiptKey)) ExceptionHelper.throwRfFulfillLogicException("收货单号不能为空");
        if(UtilHelper.isEmpty(receiptLineNumber)) ExceptionHelper.throwRfFulfillLogicException("收货单行号不能为空");
        if(UtilHelper.isEmpty(serialNumber)) ExceptionHelper.throwRfFulfillLogicException("唯一码不能为空");

        return DBHelper.getRecord(context,connection,
                "SELECT * FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SOURCELINENUMBER = ? AND IOFLAG = 'I' AND SERIALNUMBERLONG = ?",
                new Object[]{receiptKey,receiptLineNumber,serialNumber},
                "唯一码"+serialNumber+"在收货单"+receiptKey+",行号"+receiptLineNumber+"不存在",checkExist);
    }

    public static List<HashMap<String,String>> findDetailsByReceiptLineAndLpn(Context context, Connection conn, String receiptKey,String receiptLineNumber, String lpn, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(receiptKey)) ExceptionHelper.throwRfFulfillLogicException("收货单号不能为空");
        if(UtilHelper.isEmpty(receiptLineNumber)) ExceptionHelper.throwRfFulfillLogicException("收货单行号不能为空");
        if(UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("箱号不能为空");

        List<HashMap<String,String>> snList = DBHelper.executeQuery(context,conn,"select * from LotxIdDetail where SOURCEKEY = ? and SOURCELINENUMBER = ? and IOFLAG = 'I' and ID = ?", new Object[]{receiptKey,receiptLineNumber,lpn});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException(" 箱号:"+lpn+" 在收货单中不存在唯一码数据");

        return snList;
    }

    public static List<HashMap<String,String>> findDetailsByPickDetailKey(Context context, Connection conn, String pickDetailKey, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(pickDetailKey)) ExceptionHelper.throwRfFulfillLogicException("拣货明细号不能为空");

        List<HashMap<String,String>> snList = DBHelper.executeQuery(context,conn,"select * from LotxIdDetail where IOFLAG = 'O' and PICKDETAILKEY = ?", new Object[]{pickDetailKey});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException("拣货明细号:"+pickDetailKey+" 在出库单中不存在唯一码数据");

        return snList;
    }

    public static void buildReceiptLotxIdInfo(Context context, Connection connection, String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber, String[] snList) throws Exception {

        if (snList != null) {

            String lotxidKey = buildLotxIHeaderInfo(context,connection,sku,ioFlag,id,sourceKey,sourceLineNumber,""," ");

            int i=1;
            for(String sn : snList){
                buildLotxIdDetailInfo(context,connection,lotxidKey,sku,ioFlag,"",id,sourceKey,sourceLineNumber,"","",sn,i++);
            }

        }
    }
    public static void buildReceiptLotxIdInfo(Context context, Connection connection, String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber, String[] snList,String[] snWgtList,String[] snUomList) throws Exception {

        if (snList != null) {

            String lotxidKey = buildLotxIHeaderInfo(context,connection,sku,ioFlag,id,sourceKey,sourceLineNumber,""," ");

            int i=1;
            for(String sn : snList){
                buildLotxIdDetailInfo(context,connection,lotxidKey,sku,ioFlag,"",id,sourceKey,sourceLineNumber,"","",sn,snWgtList[i-1],snUomList[i-1],i++);
            }

        }
    }
    public static void buildLotxIdDetailInfo(Context context, Connection connection,String  lotxidKey, String sku, String ioFlag, String fromId, String toId, String sourceKey, String sourceLineNumber, String pickDetailKey, String lot, String sn, String snWgt,String snUom,int seq) throws Exception {


        if ("I".equals(ioFlag)) {


            HashMap<String, String> lotxIdDetail = DBHelper.getRecord(context, connection, "SELECT ID FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SERIALNUMBERLONG = ? AND IOFLAG ='I'",
                    new Object[]{ sourceKey, sn}, "唯一码", false );

            if(lotxIdDetail != null) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于当前收货单的箱号" + lotxIdDetail.get("ID") + "中,添加失败");

            Integer count = DBHelper.getValue(context, connection, "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? ",
                    new Object[]{  sku,   sn}  ,Integer.class,"唯一码");

            if(count>0) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于库存中,添加失败");

        }else  if ("O".equals(ioFlag)) {
            Integer count = DBHelper.getValue(context, connection, "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,Integer.class,"唯一码");
            if(count==0) ExceptionHelper.throwRfFulfillLogicException("箱号"+fromId+"中未找到唯一码" + sn+"，拣货失败");
        }else {
            ExceptionHelper.throwRfFulfillLogicException("错误的IOFLAG参数");
        }

        LinkedHashMap<String, String> lotxIdDetailHashMap = new LinkedHashMap<>();
        lotxIdDetailHashMap.put("WHSEID", "@user");
        lotxIdDetailHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdDetailHashMap.put("SKU", sku);
        lotxIdDetailHashMap.put("IOFLAG", ioFlag);
        lotxIdDetailHashMap.put("ID", toId);
        if(ioFlag.equals("I")) {
            lotxIdDetailHashMap.put("LOT", " ");
            lotxIdDetailHashMap.put("IOTHER1", sn);
        }else {
            lotxIdDetailHashMap.put("LOT",lot);
            lotxIdDetailHashMap.put("OOTHER1", sn);
        }
        lotxIdDetailHashMap.put("SOURCEKEY", sourceKey);
        lotxIdDetailHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdDetailHashMap.put("PICKDETAILKEY", pickDetailKey);
        lotxIdDetailHashMap.put("IQTY", "1");
        lotxIdDetailHashMap.put("OQTY", "1");
        lotxIdDetailHashMap.put("Wgt", UtilHelper.isEmpty(snWgt)?"":snWgt);
        lotxIdDetailHashMap.put("IOTHER2", UtilHelper.isEmpty(snUom)?"":snUom);
        lotxIdDetailHashMap.put("LOTXIDLINENUMBER", LegecyUtilHelper.To_Char(seq, 5));
        lotxIdDetailHashMap.put("SERIALNUMBERLONG", sn);
        String userid = context.getUserID();
        lotxIdDetailHashMap.put("ADDWHO", userid);
        lotxIdDetailHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert(context, connection, "LOTXIDDETAIL", lotxIdDetailHashMap);

    }

    public static String buildLotxIHeaderInfo(Context context, Connection connection, String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber,String pickDetailKey,String lot) throws Exception {
        String lotxidKey = KeyGen.getKey(context,"LOTXIDHEADER");

        LinkedHashMap<String, String> lotxIdHeaderHashMap = new LinkedHashMap<>();
        lotxIdHeaderHashMap.put("WHSEID", "@user");
        lotxIdHeaderHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdHeaderHashMap.put("STORERKEY", CDSysSet.getStorerKey(context, connection));
        lotxIdHeaderHashMap.put("SKU", sku);
        lotxIdHeaderHashMap.put("IOFLAG", ioFlag);
        lotxIdHeaderHashMap.put("LOT", lot);
        lotxIdHeaderHashMap.put("ID", id);
        lotxIdHeaderHashMap.put("SOURCEKEY", sourceKey);
        lotxIdHeaderHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdHeaderHashMap.put("PICKDETAILKEY", pickDetailKey);
        lotxIdHeaderHashMap.put("STATUS", "0");
        String userid = context.getUserID();
        lotxIdHeaderHashMap.put("ADDWHO", userid);
        lotxIdHeaderHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert(context, connection, "LOTXIDHEADER", lotxIdHeaderHashMap);

        return lotxidKey;

    }



    public static void buildLotxIdDetailInfo(Context context, Connection connection,String  lotxidKey, String sku, String ioFlag, String fromId, String toId, String sourceKey, String sourceLineNumber, String pickDetailKey, String lot, String sn, int seq) throws Exception {


        if ("I".equals(ioFlag)) {


            HashMap<String, String> lotxIdDetail = DBHelper.getRecord(context, connection, "SELECT ID FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SERIALNUMBERLONG = ? AND IOFLAG ='I'",
                    new Object[]{ sourceKey, sn}, "唯一码", false );

            if(lotxIdDetail != null) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于当前收货单的箱号" + lotxIdDetail.get("ID") + "中,添加失败");

            Integer count = DBHelper.getValue(context, connection, "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? ",
                    new Object[]{  sku,   sn}  ,Integer.class,"唯一码");

            if(count>0) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于库存中,添加失败");

        }else  if ("O".equals(ioFlag)) {
            Integer count = DBHelper.getValue(context, connection, "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,Integer.class,"唯一码");
            if(count==0) ExceptionHelper.throwRfFulfillLogicException("箱号"+fromId+"中未找到唯一码" + sn+"，拣货失败");
        }else {
            ExceptionHelper.throwRfFulfillLogicException("错误的IOFLAG参数");
        }

        LinkedHashMap<String, String> lotxIdDetailHashMap = new LinkedHashMap<>();
        lotxIdDetailHashMap.put("WHSEID", "@user");
        lotxIdDetailHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdDetailHashMap.put("SKU", sku);
        lotxIdDetailHashMap.put("IOFLAG", ioFlag);
        lotxIdDetailHashMap.put("ID", toId);
        if(ioFlag.equals("I")) {
            lotxIdDetailHashMap.put("LOT", " ");
            lotxIdDetailHashMap.put("IOTHER1", sn);
        }else {
            lotxIdDetailHashMap.put("LOT",lot);
            lotxIdDetailHashMap.put("OOTHER1", sn);
        }
        lotxIdDetailHashMap.put("SOURCEKEY", sourceKey);
        lotxIdDetailHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdDetailHashMap.put("PICKDETAILKEY", pickDetailKey);
        lotxIdDetailHashMap.put("IQTY", "1");
        lotxIdDetailHashMap.put("OQTY", "1");
        lotxIdDetailHashMap.put("Wgt","0");
        if (ioFlag.equals("O")&&CDSysSet.enableSNwgt(context,connection)){
            String wgt = DBHelper.getValue(context, connection, "SELECT NETWEIGHT as WGT FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,String.class,"唯一码");
            lotxIdDetailHashMap.put("Wgt",UtilHelper.isEmpty(wgt)?"0":wgt);
        }
        lotxIdDetailHashMap.put("LOTXIDLINENUMBER", LegecyUtilHelper.To_Char(seq, 5));
        lotxIdDetailHashMap.put("SERIALNUMBERLONG", sn);
        String userid = context.getUserID();
        lotxIdDetailHashMap.put("ADDWHO", userid);
        lotxIdDetailHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert(context, connection, "LOTXIDDETAIL", lotxIdDetailHashMap);

    }







}
