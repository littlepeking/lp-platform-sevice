package com.enhantec.wms.backend.common.base;


import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class LotxId {

    public static Map<String,String> findDetailByReceiptLineAndSn( String receiptKey, String receiptLineNumber, String serialNumber, boolean checkExist)throws FulfillLogicException {
        if(UtilHelper.isEmpty(receiptKey)) ExceptionHelper.throwRfFulfillLogicException("收货单号不能为空");
        if(UtilHelper.isEmpty(receiptLineNumber)) ExceptionHelper.throwRfFulfillLogicException("收货单行号不能为空");
        if(UtilHelper.isEmpty(serialNumber)) ExceptionHelper.throwRfFulfillLogicException("唯一码不能为空");

        return DBHelper.getRecord(
                "SELECT * FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SOURCELINENUMBER = ? AND IOFLAG = 'I' AND SERIALNUMBERLONG = ?",
                new Object[]{receiptKey,receiptLineNumber,serialNumber},
                "唯一码"+serialNumber+"在收货单"+receiptKey+",行号"+receiptLineNumber+"不存在",checkExist);
    }

    public static List<Map<String,String>> findDetailsByReceiptLineAndLpn( String receiptKey,String receiptLineNumber, String lpn, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(receiptKey)) ExceptionHelper.throwRfFulfillLogicException("收货单号不能为空");
        if(UtilHelper.isEmpty(receiptLineNumber)) ExceptionHelper.throwRfFulfillLogicException("收货单行号不能为空");
        if(UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("箱号不能为空");

        List<Map<String,String>> snList = DBHelper.executeQuery("select * from LotxIdDetail where SOURCEKEY = ? and SOURCELINENUMBER = ? and IOFLAG = 'I' and ID = ?", new Object[]{receiptKey,receiptLineNumber,lpn});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException(" 箱号:"+lpn+" 在收货单中不存在唯一码数据");

        return snList;
    }

    public static List<Map<String,String>> findDetailsByPickDetailKey( String pickDetailKey, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(pickDetailKey)) ExceptionHelper.throwRfFulfillLogicException("拣货明细号不能为空");

        List<Map<String,String>> snList = DBHelper.executeQuery("select * from LotxIdDetail where IOFLAG = 'O' and PICKDETAILKEY = ?", new Object[]{pickDetailKey});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException("拣货明细号:"+pickDetailKey+" 在出库单中不存在唯一码数据");

        return snList;
    }

    public static void buildReceiptLotxIdInfo( String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber, String[] snList) throws Exception {

        if (snList != null) {

            String lotxidKey = buildLotxIHeaderInfo(sku,ioFlag,id,sourceKey,sourceLineNumber,""," ");

            int i=1;
            for(String sn : snList){
                buildLotxIdDetailInfo(lotxidKey,sku,ioFlag,"",id,sourceKey,sourceLineNumber,"","",sn,i++);
            }

        }
    }
    public static void buildReceiptLotxIdInfo( String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber, String[] snList,String[] snWgtList,String[] snUomList) throws Exception {

        if (snList != null) {

            String lotxidKey = buildLotxIHeaderInfo(sku,ioFlag,id,sourceKey,sourceLineNumber,""," ");

            int i=1;
            for(String sn : snList){
                buildLotxIdDetailInfo(lotxidKey,sku,ioFlag,"",id,sourceKey,sourceLineNumber,"","",sn,snWgtList[i-1],snUomList[i-1],i++);
            }

        }
    }
    public static void buildLotxIdDetailInfo(String  lotxidKey, String sku, String ioFlag, String fromId, String toId, String sourceKey, String sourceLineNumber, String pickDetailKey, String lot, String sn, String snWgt,String snUom,int seq) throws Exception {


        if ("I".equals(ioFlag)) {


            Map<String, String> lotxIdDetail = DBHelper.getRecord( "SELECT ID FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SERIALNUMBERLONG = ? AND IOFLAG ='I'",
                    new Object[]{ sourceKey, sn}, "唯一码", false );

            if(lotxIdDetail != null) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于当前收货单的箱号" + lotxIdDetail.get("ID") + "中,添加失败");

            Integer count = DBHelper.getValue( "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? ",
                    new Object[]{  sku,   sn}  ,Integer.class,"唯一码");

            if(count>0) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于库存中,添加失败");

        }else  if ("O".equals(ioFlag)) {
            Integer count = DBHelper.getValue( "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,Integer.class,"唯一码");
            if(count==0) ExceptionHelper.throwRfFulfillLogicException("箱号"+fromId+"中未找到唯一码" + sn+"，拣货失败");
        }else {
            ExceptionHelper.throwRfFulfillLogicException("错误的IOFLAG参数");
        }

        Map<String,String> lotxIdDetailHashMap = new HashMap<>();
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
        String userid = EHContextHelper.getUser().getUsername();
        lotxIdDetailHashMap.put("ADDWHO", userid);
        lotxIdDetailHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert( "LOTXIDDETAIL", lotxIdDetailHashMap);

    }

    public static String buildLotxIHeaderInfo( String sku, String ioFlag, String id, String sourceKey, String sourceLineNumber,String pickDetailKey,String lot) throws Exception {
        String lotxidKey = IdGenerationHelper.getNCounterStr("LOTXIDHEADER");

        Map<String,String> lotxIdHeaderHashMap = new HashMap<>();
        lotxIdHeaderHashMap.put("WHSEID", "@user");
        lotxIdHeaderHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdHeaderHashMap.put("STORERKEY", CDSysSet.getStorerKey());
        lotxIdHeaderHashMap.put("SKU", sku);
        lotxIdHeaderHashMap.put("IOFLAG", ioFlag);
        lotxIdHeaderHashMap.put("LOT", lot);
        lotxIdHeaderHashMap.put("ID", id);
        lotxIdHeaderHashMap.put("SOURCEKEY", sourceKey);
        lotxIdHeaderHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdHeaderHashMap.put("PICKDETAILKEY", pickDetailKey);
        lotxIdHeaderHashMap.put("STATUS", "0");
        String userid = EHContextHelper.getUser().getUsername();
        lotxIdHeaderHashMap.put("ADDWHO", userid);
        lotxIdHeaderHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert( "LOTXIDHEADER", lotxIdHeaderHashMap);

        return lotxidKey;

    }



    public static void buildLotxIdDetailInfo(String  lotxidKey, String sku, String ioFlag, String fromId, String toId, String sourceKey, String sourceLineNumber, String pickDetailKey, String lot, String sn, int seq) throws Exception {


        if ("I".equals(ioFlag)) {


            Map<String, String> lotxIdDetail = DBHelper.getRecord( "SELECT ID FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SERIALNUMBERLONG = ? AND IOFLAG ='I'",
                    new Object[]{ sourceKey, sn}, "唯一码", false );

            if(lotxIdDetail != null) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于当前收货单的箱号" + lotxIdDetail.get("ID") + "中,添加失败");

            Integer count = DBHelper.getValue( "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? ",
                    new Object[]{  sku,   sn}  ,Integer.class,"唯一码");

            if(count>0) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于库存中,添加失败");

        }else  if ("O".equals(ioFlag)) {
            Integer count = DBHelper.getValue( "SELECT count(1) FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,Integer.class,"唯一码");
            if(count==0) ExceptionHelper.throwRfFulfillLogicException("箱号"+fromId+"中未找到唯一码" + sn+"，拣货失败");
        }else {
            ExceptionHelper.throwRfFulfillLogicException("错误的IOFLAG参数");
        }

        Map<String,String> lotxIdDetailHashMap = new HashMap<>();
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
        if (ioFlag.equals("O")&&CDSysSet.enableSNwgt()){
            String wgt = DBHelper.getValue( "SELECT NETWEIGHT as WGT FROM SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? AND ID = ? ",
                    new Object[]{ sku, sn, fromId}  ,String.class,"唯一码");
            lotxIdDetailHashMap.put("Wgt",UtilHelper.isEmpty(wgt)?"0":wgt);
        }
        lotxIdDetailHashMap.put("LOTXIDLINENUMBER", LegecyUtilHelper.To_Char(seq, 5));
        lotxIdDetailHashMap.put("SERIALNUMBERLONG", sn);
        String userid = EHContextHelper.getUser().getUsername();
        lotxIdDetailHashMap.put("ADDWHO", userid);
        lotxIdDetailHashMap.put("EDITWHO", userid);
        LegacyDBHelper.ExecInsert( "LOTXIDDETAIL", lotxIdDetailHashMap);

    }







}
