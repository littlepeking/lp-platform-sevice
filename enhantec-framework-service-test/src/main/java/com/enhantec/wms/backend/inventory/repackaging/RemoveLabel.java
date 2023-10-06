package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.util.Map;


/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHRepackagingRemoveLabel';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRepackagingRemoveLabel', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'RemoveLabel','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ID,ESIGNATUREKEY','0.10','0');

 **/


public class RemoveLabel extends WMSBaseService {

    private static final long serialVersionUID = 1L;

    public RemoveLabel()
    {

    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();



        try
        {

            final String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            final String id= serviceDataHolder.getInputDataAsMap().getString("ID");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(receiptKey)) throw new Exception("分装入库号不能为空");
            if (UtilHelper.isEmpty(id)) throw new Exception("待作废的标签号不能为空");


            Map<String,String> receiptHashMap =  Receipt.findByReceiptKey(receiptKey,true);

            /*
                RECEIPT.SUSR2 分装单关联的收货批次号
                RECEIPT.SUSR3 分装间
                RECEIPT.SUSR4 分装单关联的领料出库订单号+行号
                RECEIPT.SUSR5 分装余料信息列表，格式： 容器号1|剩余数量；容器号2|剩余数量;

             */
            String orderKeyStr = receiptHashMap.get("SUSR4");
            String orderKey = orderKeyStr.substring(0,10);
            String orderLineNumber = orderKeyStr.substring(10);

            boolean isInRepackProcess = RepackgingUtils.isInRepackProcess(orderKey,orderLineNumber);
            if(isInRepackProcess) ExceptionHelper.throwRfFulfillLogicException("分装进行中，不允许进行修改");



            Map<String,String> receiptDetailHashMap =  Receipt.findReceiptDetailByLPN(receiptKey,id,true);

            if(UtilHelper.equals(receiptDetailHashMap.get("STATUS"),"20"))
                ExceptionHelper.throwRfFulfillLogicException("标签"+id+"已作废，无需再次作废");
            else if(!UtilHelper.equals(receiptDetailHashMap.get("STATUS"),"0"))
                ExceptionHelper.throwRfFulfillLogicException("标签"+id+"不允许作废");

            DBHelper.executeUpdate("UPDATE RECEIPTDETAIL SET STATUS = '20' WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ? ",new Object[]{
                    receiptKey,  receiptDetailHashMap.get("RECEIPTLINENUMBER") });

            Udtrn UDTRN=new Udtrn();

            if(esignatureKey.indexOf(':')==-1){
                UDTRN.EsignatureKey=esignatureKey;
            }else {
                //复核
                String[] eSignatureKeys = esignatureKey.split(":");
                UDTRN.EsignatureKey=eSignatureKeys[0];
                UDTRN.EsignatureKey1=eSignatureKeys[1];
            }

            UDTRN.FROMTYPE="作废分装标签";
            UDTRN.FROMTABLENAME="RECEIPT";
            UDTRN.FROMKEY=receiptKey;
            UDTRN.FROMKEY1LABEL="分装入库单号";
            UDTRN.FROMKEY1= receiptKey;
            UDTRN.FROMKEY2LABEL="标签容器号";
            UDTRN.FROMKEY2=id;
            UDTRN.FROMKEY3="";
            UDTRN.insert( userid);

            ServiceDataMap theOutDO = new ServiceDataMap();
            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);

          
        }
        catch (Exception e)
        {
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }

    }

}
