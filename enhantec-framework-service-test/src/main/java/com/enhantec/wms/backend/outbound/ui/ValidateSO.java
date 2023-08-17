package com.enhantec.wms.backend.outbound.ui;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

public class ValidateSO extends WMSBaseService {

    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHValidateSO'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHValidateSO', 'com.enhantec.sce.outbound.order.ui', 'enhantec', 'ValidateSO', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ValidateSO() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String ORDERKEY = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            Map<String, String> orderInfo =  Orders.findByOrderKey(ORDERKEY,true);

            //john -- check if order type and quality status is match
            OrderValidationHelper.checkOrderTypeAndQualityStatusMatch4Alloc(ORDERKEY);
            OrderValidationHelper.validateFieldsBeforeShip(ORDERKEY);
            Map<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey( "ORDERTYPE", orderInfo.get("TYPE"));
            if ("Y".equalsIgnoreCase(orderTypeEntry.get("EXT_UDF_STR4"))) {
                OrderValidationHelper.validateReturnPo( ORDERKEY);
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