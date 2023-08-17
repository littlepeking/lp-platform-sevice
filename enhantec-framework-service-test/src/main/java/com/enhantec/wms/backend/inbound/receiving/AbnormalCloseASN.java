package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

public class AbnormalCloseASN extends WMSBaseService {


    /**
     * --注册方法
      delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHAbnormalCloseASN'
      insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
      values ('EHAbnormalCloseASN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'AbnormalCloseASN', 'TRUE', 'JOHN', 'JOHN'
      , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public AbnormalCloseASN() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {

        String userid = EHContextHelper.getUser().getUsername();


        try {

            serviceDataHolder.getInputDataAsMap().setAttribValue("ALLOWABNORMALCLOSE","true");
            ServiceHelper.executeService("EHCloseASN", serviceDataHolder);
          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }
    }
}