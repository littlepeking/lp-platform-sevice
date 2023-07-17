package com.enhantec.wms.backend.inbound.asn;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.base.code.CDSignatureConf;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHReceiveAll';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHReceiveAll', 'com.enhantec.sce.inbound.asn', 'enhantec', 'ReceiveAll','TRUE','ALLAN','ALLAN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ReceiptKey,ESIGNATUREKEY','0.10','0');
 **/

//set JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,address=8787,suspend=n
public class ReceiveAll extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        String receiptkey= serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
        List<HashMap<String, String>> receiptDetails = Receipt.findReceiptDetails(receiptkey,true);

        HashMap<String, String> receipt = Receipt.findByReceiptKey( receiptkey, true);

        String eSignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

        if(CDSignatureConf.confirmAsnWhenReceiveAll()){
            String isConfirmedUser1 = "";
            String isConfirmedUser2 = "";
            if(UtilHelper.isEmpty(eSignatureKey)){
            } else if(eSignatureKey.indexOf(":") == -1){
                isConfirmedUser1 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey
                }, String.class, "确认人");
            }else{
                String[] split = eSignatureKey.split(":");
                String eSignatureKey1 = split[0];
                String eSignatureKey2 = split[1];
                isConfirmedUser1 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey1
                }, String.class, "确认人");
                isConfirmedUser2 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey2
                }, String.class, "复核人");
            }
            String sql = "UPDATE RECEIPT SET ISCONFIRMED = ? ,ISCONFIRMEDUSER = ?,ISCONFIRMEDUSER2 = ? WHERE RECEIPTKEY = ?";
            DBHelper.executeUpdate(sql,new Object[]{
                    "2",
                    isConfirmedUser1,
                    isConfirmedUser2,
                    receiptkey});
            Udtrn UDTRN = new Udtrn();
            UDTRN.FROMTYPE = "确认并复核ASN";
            UDTRN.FROMTABLENAME = "RECEIPT";
            UDTRN.FROMKEY = receiptkey;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "ASN单号";
            UDTRN.CONTENT01 = receiptkey;
            UDTRN.TITLE02 = "确认人";
            UDTRN.CONTENT02 = isConfirmedUser1;
            UDTRN.TITLE03 = "复核人";
            UDTRN.CONTENT03 = isConfirmedUser2;
            try {
                UDTRN.Insert( EHContextHelper.getUser().getUsername());
            } catch (Exception e) {
                ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
            }
        }


        StringBuffer errormess = new StringBuffer();
        for(HashMap<String,String> receiptDetail:receiptDetails){

            if(!receiptDetail.get("STATUS").equals("20")
               && !receiptDetail.get("STATUS").equals("9")
               && !receiptDetail.get("STATUS").equals("11")){ //过滤掉已取消、已收货和已结的收货行
                try {

                    HashMap<String,String> lotxLocxIdRecord = LotxLocxId.findById(receiptDetail.get("TOID"),false);
                    if(lotxLocxIdRecord!=null && !CDReceiptType.isReturnTypeWithInventory(receipt.get("TYPE"))){
                        ExceptionHelper.throwRfFulfillLogicException("此容器条码在库内仍有库存，不允许重复收货");
                    }

                    //过滤掉指令行
                    if(!UtilHelper.isEmpty(receiptDetail.get("TOID"))) {

                        BigDecimal grossWgtUomQty = UOM.Std2UOMQty( receiptDetail.get("PACKKEY"), receiptDetail.get("UOM"), new BigDecimal(receiptDetail.get("GROSSWGTEXPECTED")));
                        BigDecimal tareWgtUomQty = UOM.Std2UOMQty( receiptDetail.get("PACKKEY"), receiptDetail.get("UOM"), new BigDecimal(receiptDetail.get("TAREWGTEXPECTED")));
                        BigDecimal netWgtUomQty = UOM.Std2UOMQty( receiptDetail.get("PACKKEY"), receiptDetail.get("UOM"), new BigDecimal(receiptDetail.get("QTYEXPECTED")));

                        ServiceDataMap dataMap =  new ServiceDataMap() ;
                        dataMap.setAttribValue("LPN", receiptDetail.get("TOID"));
                        dataMap.setAttribValue("RECEIPTKEY", receiptDetail.get("RECEIPTKEY"));
                        dataMap.setAttribValue("LOC", receiptDetail.get("TOLOC"));
                        dataMap.setAttribValue("GROSSWGTRECEIVED", grossWgtUomQty.toPlainString());//毛量 GROSSWGTEXPECTED
                        dataMap.setAttribValue("TAREWGTRECEIVED", tareWgtUomQty.toPlainString());//皮重 TAREWGTEXPECTED
                        dataMap.setAttribValue("NETWGTRECEIVED", netWgtUomQty.toPlainString());
                        dataMap.setAttribValue("ESIGNATUREKEY", "PL");//ESIGNATUREKEY
                        ServiceHelper.executeService( "ReceivingWithSignature",new ServiceDataHolder(dataMap));
                    }
                } catch (Exception e) {
                    errormess.append(" 容器条码为" + receiptDetail.get("TOID") + "收货失败 失败原因:" + e.getMessage());
                }
            }
        }
        if(errormess!=null&&errormess.length()!=0){
            ExceptionHelper.throwRfFulfillLogicException(errormess.toString());
        }
    }

}
