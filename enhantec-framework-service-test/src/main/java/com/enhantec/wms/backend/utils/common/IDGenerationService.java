package com.enhantec.wms.backend.utils.common;

import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;

import java.sql.Connection;

/**
 * --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHIDGenerationService'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHIDGenerationService', 'com.enhantec.sce.utils.common', 'enhantec', 'IDGenerationService', 'TRUE', 'JOHN', 'JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,KEYNAME,KEYLENGTH','0.10','0');
 */

public class IDGenerationService extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        Connection conn = null;

        try {
            String keyName = serviceDataHolder.getInputDataAsMap().getString("KEYNAME");
            String keylength = serviceDataHolder.getInputDataAsMap().getString("KEYLENGTH");
            conn = context.getConnection();
            String userid = context.getUserID();
            String key = IdGenerationHelper.generateIDByKeyName(context, conn, userid, keyName, Integer.parseInt(keylength));

            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("Key", key);

            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);


        } catch (Exception e) {

            if (e instanceof FulfillLogicException)
                throw (FulfillLogicException) e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            try{context.releaseConnection(conn);}  catch(Exception e2){}
        }

    }
}