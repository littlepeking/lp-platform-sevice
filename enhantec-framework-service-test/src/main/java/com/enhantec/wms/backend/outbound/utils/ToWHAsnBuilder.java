package com.enhantec.wms.backend.outbound.utils;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToWHAsnBuilder {


    private Context context;
    private Connection connection;
    private String toWareHouseId;
    private String userId;
    private String storerKey;
    private String receiptType;

    private ToWHAsnBuilder(){};
    public ToWHAsnBuilder(Context context, Connection connection, String toWareHouseId, String receiptType){
        this.context = context;
        this.connection = connection;
        this.toWareHouseId = toWareHouseId.toLowerCase();
        this.userId = context.getUserID();
        this.storerKey = CDSysSet.getStorerKey(context, connection);
        this.receiptType = receiptType;
    }

    /**
     * 创建收货单行
     */
    public String buildReceiptDetailInfo(String receiptKey,String sku,int seq,Map<String,String> fromIdnotesMap) throws Exception {
        String receiptLineNumber = LegecyUtilHelper.To_Char(seq, 5);
        LinkedHashMap<String, String> receiptDetail = new LinkedHashMap<>();
        receiptDetail.put("LOTTABLE01", fromIdnotesMap.get("LOTTABLE01"));
        receiptDetail.put("LOTTABLE02", fromIdnotesMap.get("LOTTABLE02"));
        receiptDetail.put("LOTTABLE03", fromIdnotesMap.get("LOTTABLE03"));
        receiptDetail.put("LOTTABLE04", fromIdnotesMap.get("LOTTABLE04"));
        receiptDetail.put("LOTTABLE05", fromIdnotesMap.get("LOTTABLE05"));
        receiptDetail.put("LOTTABLE06", createReceiptLot(sku));
        receiptDetail.put("LOTTABLE07", fromIdnotesMap.get("LOTTABLE07"));
        receiptDetail.put("LOTTABLE08", fromIdnotesMap.get("LOTTABLE08"));
        receiptDetail.put("LOTTABLE09", fromIdnotesMap.get("LOTTABLE09"));
        receiptDetail.put("LOTTABLE10", fromIdnotesMap.get("LOTTABLE10"));
        receiptDetail.put("LOTTABLE11", fromIdnotesMap.get("LOTTABLE11"));
        receiptDetail.put("LOTTABLE12", fromIdnotesMap.get("LOTTABLE12"));

        receiptDetail.put("ELOTTABLE01", fromIdnotesMap.get("ELOTTABLE01"));
        receiptDetail.put("ELOTTABLE02", fromIdnotesMap.get("ELOTTABLE02"));
        receiptDetail.put("ELOTTABLE03", fromIdnotesMap.get("ELOTTABLE03"));
        receiptDetail.put("ELOTTABLE04", fromIdnotesMap.get("ELOTTABLE04"));
        receiptDetail.put("ELOTTABLE05", fromIdnotesMap.get("ELOTTABLE05"));
        receiptDetail.put("ELOTTABLE06", fromIdnotesMap.get("ELOTTABLE06"));
        receiptDetail.put("ELOTTABLE07", fromIdnotesMap.get("ELOTTABLE07"));
        receiptDetail.put("ELOTTABLE08", fromIdnotesMap.get("ELOTTABLE08"));//经销商代码/供应商代码
        receiptDetail.put("ELOTTABLE09", fromIdnotesMap.get("ELOTTABLE09"));
        receiptDetail.put("ELOTTABLE11", fromIdnotesMap.get("ELOTTABLE11"));
        receiptDetail.put("ELOTTABLE12", fromIdnotesMap.get("ELOTTABLE12"));
        receiptDetail.put("ELOTTABLE13", fromIdnotesMap.get("ELOTTABLE13"));
        receiptDetail.put("ELOTTABLE14", fromIdnotesMap.get("ELOTTABLE14"));
        receiptDetail.put("ELOTTABLE15", fromIdnotesMap.get("LOTTABLE06"));//上一批号，记录移库前的物料批号
        receiptDetail.put("ELOTTABLE16", fromIdnotesMap.get("ELOTTABLE16"));
        receiptDetail.put("ELOTTABLE17", fromIdnotesMap.get("ELOTTABLE17"));
        receiptDetail.put("ELOTTABLE18", fromIdnotesMap.get("ELOTTABLE18"));
        receiptDetail.put("ELOTTABLE19", fromIdnotesMap.get("ELOTTABLE19"));
        receiptDetail.put("ELOTTABLE20", fromIdnotesMap.get("ELOTTABLE20"));
        receiptDetail.put("ELOTTABLE21", fromIdnotesMap.get("ELOTTABLE21"));

        receiptDetail.put("SUSR6",fromIdnotesMap.get("PROJECTCODE"));


        receiptDetail.put("GROSSWGTEXPECTED", fromIdnotesMap.get("GROSSWGT"));
        receiptDetail.put("QTYEXPECTED", fromIdnotesMap.get("NETWGT"));
        receiptDetail.put("TAREWGTEXPECTED", fromIdnotesMap.get("TAREWGT"));
        receiptDetail.put("BARRELNUMBER", fromIdnotesMap.get("BARRELNUMBER"));
        receiptDetail.put("TOTALBARRELNUMBER", fromIdnotesMap.get("TOTALBARREL"));

        receiptDetail.put("EXTERNRECEIPTKEY", "WMS"+receiptKey);
        receiptDetail.put("RECEIPTKEY", receiptKey);
        receiptDetail.put("RECEIPTLINENUMBER", receiptLineNumber);
        receiptDetail.put("TYPE", receiptType);
        receiptDetail.put("STORERKEY", storerKey);
        receiptDetail.put("SKU", sku);
        receiptDetail.put("UOM",fromIdnotesMap.get("UOM"));
        receiptDetail.put("PACKKEY",fromIdnotesMap.get("PACKKEY"));
        receiptDetail.put("TOLOC", "STAGE");
        receiptDetail.put("TOID", fromIdnotesMap.get("ID"));
        receiptDetail.put("CONDITIONCODE", "OK");
        receiptDetail.put("ADDWHO", userId);
        receiptDetail.put("EDITWHO", userId);
        receiptDetail.put("NOTES", fromIdnotesMap.get("NOTES"));

        LegacyDBHelper.ExecInsert(context, connection, toWareHouseId+".RECEIPTDETAIL", receiptDetail);

        return receiptLineNumber;
    }

    private String createReceiptLot(String sku) throws Exception
    {
        HashMap<String,String> lotRuleCodelkup = DBHelper.getRecord(context,connection,
                "SELECT * FROM "+toWareHouseId+".CODELKUP WHERE LISTNAME = ? AND CODE = ?",
                new Object[]{"SYSSET","WAREHOUSE"},"");
        if(!IdGenerationHelper.checkWareHouseOrBoxPrefixConf(lotRuleCodelkup)) throw new Exception("SYSSET.WAREHOUSE配置不符合要求");
        String warehouseCode = UtilHelper.nvl(lotRuleCodelkup.get("UDF1"),"");
        String skuTypeCode= LegacyDBHelper.GetValue(context, connection, "select udf4 from "+toWareHouseId+".codelkup a,sku s where a.listname=? and a.code=s.busr4 and s.sku=?", new String[]{"SKUTYPE1",sku}, "");//根据sku获批号是sm||sc
        String CurDate= LegacyDBHelper.GetValue(context, connection, " select FORMAT(getdate(), ?)", new String[]{UtilHelper.nvl(lotRuleCodelkup.get("UDF2")," ")}, "");
        String prefix = warehouseCode+skuTypeCode+CurDate;
        String num = String.valueOf(getKeyCount(prefix,1));
        while (num.length()<Integer.parseInt(lotRuleCodelkup.get("UDF3"))) num="0"+num;
        return  prefix + num;
    }

    /**
     * 创建收货头
     */
    public String buildReceiptHeadInfo(String isConfirmedUser1,String isConfirmedUser2) throws Exception {
        String receiptKey = getKeyCount("RECEIPT",10);
        LinkedHashMap<String, String> receipt = new LinkedHashMap<>();
        receipt.put("RECEIPTKEY", receiptKey);
        receipt.put("EXTERNRECEIPTKEY", "WMS" + receiptKey);
        receipt.put("ADDWHO", userId);
        receipt.put("EDITWHO", userId);
        receipt.put("STATUS", "0");
        receipt.put("ALLOWAUTORECEIPT", "0");
        receipt.put("TYPE", receiptType);
        receipt.put("STORERKEY", storerKey);
        receipt.put("ISCONFIRMED","2");
        receipt.put("ISCONFIRMEDUSER",isConfirmedUser1);
        receipt.put("ISCONFIRMEDUSER2",isConfirmedUser2);
        LegacyDBHelper.ExecInsert(context, connection, toWareHouseId + ".RECEIPT", receipt);
        return receiptKey;
    }

    private String getKeyCount(String keyName,int length){
        String result;
        result = DBHelper.getValue(context, connection,
                "SELECT KEYCOUNT FROM " + toWareHouseId + ".NCOUNTER WHERE KEYNAME = ?",
                new Object[]{keyName}, "",false);
        if(UtilHelper.isEmpty(result)){
            DBHelper.executeUpdate(context,connection,
                    "INSERT INTO "+toWareHouseId+".NCOUNTER (KEYNAME,KEYCOUNT) VALUES (?,?)",
                    new Object[]{keyName,"1"});
            result = "0";
        }
        result = String.valueOf(Integer.parseInt(result)+1);

        while(result.length()<length) result="0"+result;
         DBHelper.executeUpdate(context,connection,
                "update "+toWareHouseId+".ncounter set KeyCount= KeyCount + 1 where KeyName=?",
                new Object[]{keyName});
        return result;
    }

    /**
     * 创建唯一码收货头
     */
    private String buildReceiptLotXIdHeaderInfo(String sku,  String id, String sourceKey, String sourceLineNumber) throws Exception {
        String lotxidKey = this.getKeyCount("LOTXIDHEADER",10);
        LinkedHashMap<String, String> lotxIdHeaderHashMap = new LinkedHashMap<>();
        lotxIdHeaderHashMap.put("WHSEID", "@user");
        lotxIdHeaderHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdHeaderHashMap.put("STORERKEY", storerKey);
        lotxIdHeaderHashMap.put("SKU", sku);
        lotxIdHeaderHashMap.put("IOFLAG", "I");
        lotxIdHeaderHashMap.put("LOT", " ");
        lotxIdHeaderHashMap.put("ID", id);
        lotxIdHeaderHashMap.put("SOURCEKEY", sourceKey);
        lotxIdHeaderHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdHeaderHashMap.put("PICKDETAILKEY", "");
        lotxIdHeaderHashMap.put("STATUS", "0");
        lotxIdHeaderHashMap.put("ADDWHO", userId);
        lotxIdHeaderHashMap.put("EDITWHO", userId);
        LegacyDBHelper.ExecInsert(context, connection, toWareHouseId+".LOTXIDHEADER", lotxIdHeaderHashMap);
        return lotxidKey;
    }

    /**
     * 创建唯一码收货行
     */
    private void buildReceiptLotxIdDetailInfo(String  lotxidKey, String sku, String id, String sourceKey, String sourceLineNumber, String sn,String snWgt,String snUom, int seq) throws Exception {
        HashMap<String, String> lotxIdDetail = DBHelper.getRecord(context, connection,
                "SELECT ID FROM "+toWareHouseId+".LOTXIDDETAIL WHERE SOURCEKEY = ? AND SERIALNUMBERLONG = ? AND IOFLAG ='I'",
                new Object[]{ sourceKey, sn}, "唯一码", false );
        if(lotxIdDetail != null) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于当前收货单的箱号" + lotxIdDetail.get("ID") + "中,添加失败");

        Integer count = DBHelper.getValue(context, connection,
                "SELECT count(1) FROM "+toWareHouseId+".SERIALINVENTORY WHERE SKU = ? and SERIALNUMBERLONG = ? ",
                new Object[]{  sku,   sn}  ,Integer.class,"唯一码");
        if(count>0) ExceptionHelper.throwRfFulfillLogicException("唯一码" + sn + "已经存在于库存中,添加失败");

        LinkedHashMap<String, String> lotxIdDetailHashMap = new LinkedHashMap<>();
        lotxIdDetailHashMap.put("WHSEID", "@user");
        lotxIdDetailHashMap.put("LOTXIDKEY", lotxidKey);
        lotxIdDetailHashMap.put("SKU", sku);
        lotxIdDetailHashMap.put("IOFLAG", "I");
        lotxIdDetailHashMap.put("ID", id);
        lotxIdDetailHashMap.put("LOT", " ");
        lotxIdDetailHashMap.put("IOTHER1", sn);
        lotxIdDetailHashMap.put("SOURCEKEY", sourceKey);
        lotxIdDetailHashMap.put("SOURCELINENUMBER", sourceLineNumber);
        lotxIdDetailHashMap.put("PICKDETAILKEY", "");
        lotxIdDetailHashMap.put("IQTY", "1");
        lotxIdDetailHashMap.put("OQTY", "1");
        lotxIdDetailHashMap.put("Wgt", UtilHelper.isEmpty(snWgt)?"":snWgt);
        lotxIdDetailHashMap.put("IOTHER2", UtilHelper.isEmpty(snUom)?"":snUom);
        lotxIdDetailHashMap.put("LOTXIDLINENUMBER", LegecyUtilHelper.To_Char(seq, 5));
        lotxIdDetailHashMap.put("SERIALNUMBERLONG", sn);
        lotxIdDetailHashMap.put("ADDWHO", userId);
        lotxIdDetailHashMap.put("EDITWHO", userId);
        LegacyDBHelper.ExecInsert(context, connection, toWareHouseId+".LOTXIDDETAIL", lotxIdDetailHashMap);
    }

    /**
     * 创建唯一码收货列表
     */
    public void buildReceiptLotxIdInfo(String sku,String id, String sourceKey, String sourceLineNumber, String[] snList,String[] snWgtList,String[] snUomList) throws Exception {
        if (snList != null) {
            String lotxidKey = buildReceiptLotXIdHeaderInfo(sku,id,sourceKey,sourceLineNumber);
            int i = 0;
            for(String sn : snList){
                buildReceiptLotxIdDetailInfo(lotxidKey,sku,id,sourceKey,sourceLineNumber,sn,snWgtList[i],snUomList[i],i++);
            }
        }
    }

}
