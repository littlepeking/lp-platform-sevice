package com.enhantec.wms.backend.outbound.ship;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CloseSO extends LegacyBaseService {


    /**
     * --注册方法
      delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHCloseSO'
      insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
      values ('EHCloseSO', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'CloseSO', 'TRUE', 'JOHN', 'JOHN'
      , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */

    //用于订单发货前，操作人员手工结算订单，结算后，系统自动扣减订单行未结数量中未分配或者未拣货的数量

    private static final long serialVersionUID = 1L;

    public CloseSO() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String notes = DBHelper.getValue("SELECT NOTES FROM Esignature WHERE SERIALKEY = ?",new Object[]{
                    esignatureKey},String.class,"电子签名");

            Map<String, String>  orderInfo = Orders.findByOrderKey(orderKey,true);

            List<Map<String, String>>  pickDetailList = PickDetail.findByOrderKey(orderKey,false);

            if(pickDetailList.stream().anyMatch(x-> "3".equals(x.get("STATUS")))) throw new Exception("存在处理中状态的拣货项，请完成或删除该拣货项后再关闭订单");


                ServiceDataHolder serviceDataHolder4CloseOrder = new ServiceDataHolder();

            ServiceDataMap dataMap = new ServiceDataMap();

            dataMap.setData( new HashMap<String,Object>(){{
                put("ORDERKEY",orderKey);
                put("ORDERLINENUMBER","ALL");
                put("ALLOWBACKORDER","false");
                put("ORDERSTATUS",orderInfo.get("STATUS"));
                put("BACKORDERTYPE","12");//NOT IN USE, SET DEFAULT VALUE
            }});

            serviceDataHolder4CloseOrder.setInputData(dataMap);

            ServiceHelper.executeService("NSPCLOSEORDER", serviceDataHolder4CloseOrder);

            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=esignatureKey;
            UDTRN.FROMTYPE="结算订单";
            UDTRN.FROMTABLENAME="ORDERS";
            UDTRN.FROMKEY=orderKey;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="订单号";    UDTRN.CONTENT01=orderKey;
            UDTRN.Insert( userid);

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());

        }finally {
            
        }
    }
}