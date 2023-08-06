
package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.List;

public class ConfirmASN extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHConfirmASN'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHConfirmASN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'ConfirmASN', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ConfirmASN() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {

        String userid = EHContextHelper.getUser().getUsername();


        try {



            String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            Map<String, String>  receiptInfo =  Receipt.findByReceiptKey(receiptKey,true);

            if ("2".equals(receiptInfo.get("ISCONFIRMED"))) {
                ExceptionHelper.throwRfFulfillLogicException("订单已为复核状态，无需重复确认");
            }

            List<Map<String, String>>  receiptDetails = Receipt.findReceiptDetails(receiptKey,false);

            if (receiptDetails.size()==0) {
                ExceptionHelper.throwRfFulfillLogicException("没有找到该ASN单的收货明细，不允许确认");
            }

            //进行复核确认时为ASN生成收货批次
            if(receiptInfo.get("ISCONFIRMED").equals("1")) {

                Map<String, String> receiptTypeInfo = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptInfo.get("TYPE"));


                for (Map<String, String> receiptDetail : receiptDetails) {
                    //为ASN指令行生成收货批次
                    if (Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receiptTypeInfo.get("UDF5"))
                            && UtilHelper.isEmpty(receiptDetail.get("TOID"))
                            && UtilHelper.isEmpty(receiptDetail.get("LOTTABLE06"))) {

                        String receiptLot = IdGenerationHelper.createReceiptLot( receiptDetail.get("SKU"));
                        DBHelper.executeUpdate( "UPDATE RECEIPTDETAIL SET LOTTABLE06 = ? WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ?",
                                new Object[]{
                                        receiptLot,
                                        receiptDetail.get("RECEIPTKEY"),
                                        receiptDetail.get("RECEIPTLINENUMBER")
                                });
                    }

                }
            }

            //1) 初始状态： 确认按钮为可编辑状态； 取消按钮为不可编辑状态；
            //2）点击确认按钮：弹出电子签名，验证成功后复核状态变更为已确认；
            // 确认按钮为可编辑状态； 取消按钮为可编辑状态；
            //3）再次点击确认按钮，弹出电子签名，验证陈宫后复核状态变更为已复核。
            // 确认按钮为不可编辑状态； 取消按钮为可编辑状态；
            //4） 点击取消确认，弹出电子签名，验证成功后复核状态变更为未确认；

            //0：未确认 1：已确认 2：已复核

            if(esignatureKey.indexOf(':')==-1) {

                String isConfirmedUser = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        esignatureKey
                }, String.class, "复核人");

                //复核操作校验
                if (!UtilHelper.isEmpty(receiptInfo.get("ISCONFIRMEDUSER"))) {

                    if (receiptInfo.get("ISCONFIRMEDUSER").equals(isConfirmedUser)) {
                        ExceptionHelper.throwRfFulfillLogicException("复核人和确认人不能为同一人");
                    } else {
                        //复核操作
                        DBHelper.executeUpdate( "UPDATE RECEIPT SET ISCONFIRMEDUSER2 = ? , ISCONFIRMED = 2 WHERE RECEIPTKEY = ? ",
                                new Object[]{isConfirmedUser, receiptKey});
                    }
                } else {
                    //确认操作
                    DBHelper.executeUpdate( "UPDATE RECEIPT SET ISCONFIRMEDUSER = ? , ISCONFIRMED = 1 WHERE RECEIPTKEY = ? ",
                            new Object[]{isConfirmedUser, receiptKey});
                }


                Udtrn UDTRN = new Udtrn();

                if (receiptInfo.get("ISCONFIRMED").equals("0")) {
                    UDTRN.EsignatureKey = esignatureKey;
                } else {//复核
                    UDTRN.EsignatureKey1 = esignatureKey;
                }

                UDTRN.FROMTYPE = receiptInfo.get("ISCONFIRMED").equals("0") ? "确认ASN" : "复核ASN";
                UDTRN.FROMTABLENAME = "RECEIPT";
                UDTRN.FROMKEY = receiptKey;
                UDTRN.FROMKEY1 = "";
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "ASN单号";
                UDTRN.CONTENT01 = receiptKey;
                UDTRN.TITLE02 = "操作人";
                UDTRN.CONTENT02 = isConfirmedUser
                ;
                UDTRN.Insert( userid);

            }else{

                String[] eSignatureKeys = esignatureKey.split(":");
                String eSignatureKey1=eSignatureKeys[0];
                String eSignatureKey2=eSignatureKeys[1];

                String isConfirmedUser1 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey1
                }, String.class, "确认人");

                String isConfirmedUser2 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey2
                }, String.class, "复核人");


                //确认操作
                DBHelper.executeUpdate( "UPDATE RECEIPT SET ISCONFIRMEDUSER = ? , ISCONFIRMEDUSER2 = ? , ISCONFIRMED = 2 WHERE RECEIPTKEY = ? ",
                        new Object[]{isConfirmedUser1, isConfirmedUser2, receiptKey});


                Udtrn UDTRN = new Udtrn();
                UDTRN.FROMTYPE = "确认并复核ASN";
                UDTRN.FROMTABLENAME = "RECEIPT";
                UDTRN.FROMKEY = receiptKey;
                UDTRN.FROMKEY1 = "";
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "ASN单号";
                UDTRN.CONTENT01 = receiptKey;
                UDTRN.TITLE02 = "确认人";
                UDTRN.CONTENT02 = isConfirmedUser1;
                UDTRN.TITLE02 = "复核人";
                UDTRN.CONTENT02 = isConfirmedUser2;
                UDTRN.Insert( userid);

            }

          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
}