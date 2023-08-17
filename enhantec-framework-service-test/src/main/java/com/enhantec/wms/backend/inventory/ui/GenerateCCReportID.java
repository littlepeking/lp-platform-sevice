package com.enhantec.wms.backend.inventory.ui;


import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;

/**
 * --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHGenerateCCReportID'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHGenerateCCReportID', 'com.enhantec.sce.inventory.ui', 'enhantec', 'GenerateCCReportID', 'TRUE', 'JOHN', 'JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ESIGNATUREKEY','0.10','0');
 */

public class GenerateCCReportID extends WMSBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {



        try {


            String reportKey = IdGenerationHelper.generateID( "CCREPORTKEY", "", 10);

            DBHelper.executeUpdate( "update cc set REPORTKEY = ? where REPORTKEY is null ", new Object[]{
                    reportKey});

        } catch (Exception e) {

            if (e instanceof FulfillLogicException)
                throw (FulfillLogicException) e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }


    }
}