
package com.enhantec.wms.backend.outbound.ui;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;
import java.util.HashMap;

public class ClientReceivedStatus extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='ClientReceivedStatus'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('ClientReceivedStatus', 'com.enhantec.sce.outbound.order.ui', 'enhantec', 'ClientReceivedStatus', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ClientReceivedStatus() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = context.getUserID();


        try {



            String ORDERKEY = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");


            HashMap<String, String>  ORDERSInfo = Orders.findByOrderKey(context, ORDERKEY, true);
            String status=ORDERSInfo.get("STATUS");
            if (!"95".equals(status)){
                ExceptionHelper.throwRfFulfillLogicException("当前状态不能进行此操作");
            }

            DBHelper.executeUpdate(context, "UPDATE ORDERS SET ClientReceivedStatus = ?  WHERE ORDERKEY = ? ",
                            new Object[]{"1", ORDERKEY });

            Udtrn UDTRN=new Udtrn();


                UDTRN.EsignatureKey = esignatureKey;


            UDTRN.FROMTYPE="确认客户签收";
            UDTRN.FROMTABLENAME="ORDERS";
            UDTRN.FROMKEY=ORDERKEY;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="出库订单号";    UDTRN.CONTENT01=ORDERKEY;
            UDTRN.TITLE02="是否签收";    UDTRN.CONTENT02="是";
            UDTRN.Insert(context, userid);


        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
}