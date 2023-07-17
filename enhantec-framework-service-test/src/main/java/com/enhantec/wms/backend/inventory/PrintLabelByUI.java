package com.enhantec.wms.backend.inventory;

import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHPrintLabelByUI';

 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHPrintLabelByUI', 'com.enhantec.sce.inventory', 'enhantec', 'PrintLabelByUI','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,lpn,labelName,printerName,copies,esignaturekey','0.10','0');

 **/


public class PrintLabelByUI extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {



        try {

            

            String lpns = serviceDataHolder.getInputDataAsMap().getString("lpn");
            String labelName = serviceDataHolder.getInputDataAsMap().getString("labelName");
            String printerName = serviceDataHolder.getInputDataAsMap().getString("printerName");
            String copies = serviceDataHolder.getInputDataAsMap().getString("copies");
            String[] lpnArray=lpns.split(",");
            for (String lpn : lpnArray){
                PrintHelper.printLPNByIDNotes( lpn, Labels.LPN_UI_SY, printerName, copies, "物料标签");
            }

        }catch (Exception e) {
            ExceptionHelper.throwRfFulfillLogicException("发生错误:"+e.getMessage());
        }
    }

}
