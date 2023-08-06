package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHCancelRepackaging';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHCancelRepackaging', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'CancelRepackaging','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER,ESIGNATUREKEY','0.10','0');

 **/

public class CancelRepackaging extends LegacyBaseService {

    private static final long serialVersionUID = 1L;

    public CancelRepackaging()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();



        try
        {

            final String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            final String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(orderKey)) throw new Exception("订单号不能为空");
            if (UtilHelper.isEmpty(orderLineNumber)) throw new Exception("订单行号不能为空");
            if (UtilHelper.isEmpty(esignatureKey)) throw new Exception("电子签名不能为空");

            Map<String,String> orderDetailHashMap = Orders.findOrderDetailByKey(orderKey,orderLineNumber,true);

            String repackReceiptKey = orderDetailHashMap.get("SUSR1");

            String rePackOrderKey = orderDetailHashMap.get("SUSR2");

            String currentPackLoc = orderDetailHashMap.get("SUSR3");


            if(UtilHelper.isEmpty(repackReceiptKey)) ExceptionHelper.throwRfFulfillLogicException("分装收货单不存在，无需取消");

            if(!UtilHelper.isEmpty(rePackOrderKey)) ExceptionHelper.throwRfFulfillLogicException("分装在执行过程中，不允许取消");

            Map<String,String> receiptHashMap = Receipt.findByReceiptKey( repackReceiptKey,true);

            String lottable06 =receiptHashMap.get("SUSR2");

            // SUSR1 分装入库单号
            // SUSR2 分装出库单号
            // SUSR4 已分装完成的单据列表, 格式:  收货批次1|分装入库单号1|分装出库单号1 ; 收货批次2|分装入库单号2分装出库单号2;
            DBHelper.executeUpdate("UPDATE ORDERDETAIL SET SUSR1 = null WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ",new Object[]{
                    orderKey ,  orderLineNumber });

            DBHelper.executeUpdate("UPDATE RECEIPTDETAIL SET STATUS = '20' WHERE RECEIPTKEY = ? AND STATUS = '0' ",new Object[]{
                    repackReceiptKey  });

            DBHelper.executeUpdate("UPDATE RECEIPT SET STATUS = '20' WHERE RECEIPTKEY = ? ",new Object[]{
                    repackReceiptKey  });


            Udtrn UDTRN = new Udtrn();

            UDTRN.EsignatureKey = esignatureKey;
            UDTRN.FROMTYPE = "取消分装";
            UDTRN.FROMTABLENAME = "ORDERS";
            UDTRN.FROMKEY = currentPackLoc;
            UDTRN.FROMKEY1 = orderKey + orderLineNumber;
            UDTRN.FROMKEY2 = repackReceiptKey;
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "分装批次";
            UDTRN.CONTENT01 = lottable06;
            UDTRN.TITLE02 = "出库单号及行号";
            UDTRN.CONTENT02 = orderKey + orderLineNumber;
            UDTRN.TITLE03 = "分装入库单号";
            UDTRN.CONTENT03 = repackReceiptKey;
            UDTRN.Insert( userid);

          
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