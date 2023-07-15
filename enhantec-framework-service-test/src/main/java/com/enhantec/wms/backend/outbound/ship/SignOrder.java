package com.enhantec.wms.backend.outbound.ship;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;
import java.util.HashMap;

/**
 --注册方法

 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHSignOrder'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHSignOrder', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'SignOrder', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,orderkey,esignaturekey','0.10','0');


 */

public class SignOrder extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {



        try {

            String userid = context.getUserID();



            String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderkey");

            HashMap<String, String> orderInfo = Orders.findByOrderKey(context, orderKey, true);

            if (!orderInfo.get("STATUS").equals("95")) {
                ExceptionHelper.throwRfFulfillLogicException("不能签收未发运的订单");
            }

            String esignaturekey = serviceDataHolder.getInputDataAsMap().getString("esignaturekey");

            String signedUser = DBHelper.getValue(context,"SELECT SIGN FROM Esignature WHERE SERIALKEY = ?",new Object[]{
                    esignaturekey},String.class, "电子签名");

            DBHelper.executeUpdate(context
                    , "UPDATE orders SET SUSR5 = ? where orderkey = ? ",
                    new Object[]{signedUser, orderKey});


            Udtrn UDTRN = new Udtrn();
            UDTRN.EsignatureKey = esignaturekey;
            UDTRN.FROMTYPE = "订单签收";
            UDTRN.FROMTABLENAME = "ORDERS";
            UDTRN.FROMKEY = orderInfo.get("ORDERKEY");
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.TITLE01 = "签收人";
            UDTRN.CONTENT01 = signedUser;



            UDTRN.Insert(context, context.getUserID());


        }catch (Exception e)
        {
           if ( e instanceof FulfillLogicException)
               throw (FulfillLogicException)e;
           else
               throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }
    }

}
