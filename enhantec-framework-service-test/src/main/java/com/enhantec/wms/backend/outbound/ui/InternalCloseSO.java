package com.enhantec.wms.backend.outbound.ui;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.OutboundUtils;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

public class InternalCloseSO extends LegacyBaseService {

    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHInternalCloseSO'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHInternalCloseSO', 'com.enhantec.sce.outbound.order.ui', 'enhantec', 'InternalCloseSO', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public InternalCloseSO() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            Map<String, String> orderHashMap = Orders.findByOrderKey(orderKey,true);

            if(OutboundUtils.isOrderCanBeCancelled(orderHashMap.get("STATUS"))){
                //order状态是内部创建或者外部创建时，点击"内部取消"按钮，订单状态改成"内部取消"
                OutboundUtils.cancelOrder(orderKey,false);

                Udtrn UDTRN = new Udtrn();
                UDTRN.EsignatureKey = esignatureKey;
                UDTRN.FROMTYPE = "内部取消订单";
                UDTRN.FROMTABLENAME = "ORDERS";
                UDTRN.FROMKEY = orderKey;
                UDTRN.FROMKEY1 = "";
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "订单号";
                UDTRN.CONTENT01 = orderKey;
                UDTRN.Insert( userid);

    
            }else{
                ExceptionHelper.throwRfFulfillLogicException("当前状态不允许进行内部取消，请确认当前订单没有被分配、拣货或者发运");
            }



        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());

        }finally {
            
        }
    }

}