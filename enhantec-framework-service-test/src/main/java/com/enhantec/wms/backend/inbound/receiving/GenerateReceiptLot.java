package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class GenerateReceiptLot extends LegacyBaseService {


    /**
     * --注册方法
      delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHGenerateReceiptLot'
      insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
      values ('EHGenerateReceiptLot', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'GenerateReceiptLot', 'TRUE', 'JOHN', 'JOHN'
      , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public GenerateReceiptLot() {
    }

    @Deprecated
    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = context.getUserID();


        try {



            String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");

            HashMap<String, String> receiptHashmap = Receipt.findByReceiptKey(context,receiptKey,true);

            if(!receiptHashmap.get("ISCONFIRMED").equals("0")) ExceptionHelper.throwRfFulfillLogicException("已确认或复核的单据不允许生成收货批次");

            String exReceiptKey = receiptHashmap.get("EXTERNRECEIPTKEY");

            boolean isNotPOReceiptType = UtilHelper.isEmpty(exReceiptKey) || exReceiptKey.startsWith("WMS");

            //增加ASN类型的判断
            if(isNotPOReceiptType){
                List<HashMap<String, String>> receiptGroupBySku = DBHelper.executeQuery(context,
                        "SELECT SKU,MAX(LOTTABLE06) AS LOTTABLE06 FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? GROUP BY SKU",
                        new Object[]{receiptKey});
                for (HashMap<String, String> receiptDetail : receiptGroupBySku) {
                    if(!SKU.isSerialControl(context,receiptDetail.get("SKU"))){
                        String lottable06 = receiptDetail.get("LOTTABLE06");
                        if(UtilHelper.isEmpty(lottable06)){
                            lottable06 = IdGenerationHelper.createReceiptLot(context,receiptDetail.get("SKU"));
                        }
                        populateReceiptDetails(context,receiptKey, lottable06,receiptDetail.get("SKU"));
                    }
                }
            }else{
                HashMap<String,String> warehousePrefixConfig = CodeLookup.getCodeLookupByKey(context,"SYSSET","WAREHOUSE");
                if(!exReceiptKey.startsWith(warehousePrefixConfig.get("UDF1"))) ExceptionHelper.throwRfFulfillLogicException("收货批号识别出错，正确的批号前缀应为"+warehousePrefixConfig.get("UDF1"));
                populateReceiptDetails(context,receiptKey, exReceiptKey,null);

            }


          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }

    private void populateReceiptDetails(Context context,String receiptKey, String receiptLot,String sku) throws Exception {

        List<HashMap<String, String>> receiptDetails;
        if(null == sku || UtilHelper.isEmpty(sku)) {
            receiptDetails = Receipt.findReceiptDetails(context, receiptKey, true);
        }else{
            receiptDetails = DBHelper.executeQuery(context,
                    "SELECT * FROM RECEIPTDETAIL WHERE SKU = ? AND RECEIPTKEY = ?",
                    new Object[]{sku, receiptKey});
        }

        IdGenerationHelper.resetNCounter(context, receiptLot);
        String totalBarrel = String.valueOf(receiptDetails.size());
        while (totalBarrel.length()<3) totalBarrel="0"+totalBarrel;
        int barrel = 0;
        for(HashMap<String, String> receiptDetail: receiptDetails){
            barrel++;
            String tempLpn;
            if(UtilHelper.isEmpty(receiptDetail.get("TOID"))) {
                tempLpn = IdGenerationHelper.generateLpn(context, receiptLot);
            }else{
                tempLpn = receiptDetail.get("TOID");
            }
            String barrelNumber = String.valueOf(barrel);
            while(barrelNumber.length() < 3) barrelNumber = "0" + barrelNumber;
            DBHelper.executeUpdate(context,"UPDATE RECEIPTDETAIL " +
                    "SET LOTTABLE06 = ? , BARRELNUMBER = ?, TOTALBARRELNUMBER = ?, TOID = ? " +
                    "WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ?", new Object[]{
                        receiptLot,
                        barrelNumber,
                        totalBarrel,
                        tempLpn,
                        receiptKey,
                        receiptDetail.get("RECEIPTLINENUMBER"),
            });
        }
    }
}