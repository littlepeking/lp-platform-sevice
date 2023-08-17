package com.enhantec.wms.backend.inventory;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

import java.util.Map;

/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHCCPostTask';

 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHCCPostTask', 'com.enhantec.sce.inventory', 'enhantec', 'CCPostTask','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,taskdetailkey,ccqty,uom,esignaturekey','0.10','0');

 **/


public class CCPostTask extends WMSBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {



        try {
             

            String taskDetailKey = serviceDataHolder.getInputDataAsMap().getString("taskdetailkey");

            Map<String,String> taskDetailHashMap = TaskDetail.findById(taskDetailKey,true);

            String storerKey =  taskDetailHashMap.get("STORERKEY");
            String sku =  taskDetailHashMap.get("SKU");
            Map<String,String>  skuHashMap = SKU.findById(sku,true);
            String lot =  taskDetailHashMap.get("LOT");
            String loc =  taskDetailHashMap.get("FROMLOC");
            String id =  taskDetailHashMap.get("FROMID");
            Map<String,String> idNotesInfo = IDNotes.findById(id,true);
            String packKey =  idNotesInfo.get("PACKKEY");
            String uom =   serviceDataHolder.getInputDataAsMap().getString("uom");
            String qty =  serviceDataHolder.getInputDataAsMap().getString("ccqty");


            //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ttm,
            //taskdetailkey,storerkey,sku,lot,loc,chkdigit,id,qty,packkey,uom
            serviceDataHolder.getInputDataAsMap().setAttribValue("storerkey",storerKey);
            serviceDataHolder.getInputDataAsMap().setAttribValue("sku",sku);
            serviceDataHolder.getInputDataAsMap().setAttribValue("lot",lot);
            serviceDataHolder.getInputDataAsMap().setAttribValue("loc",loc);
            serviceDataHolder.getInputDataAsMap().setAttribValue("id",id);
            serviceDataHolder.getInputDataAsMap().setAttribValue("packKey",packKey);
            serviceDataHolder.getInputDataAsMap().setAttribValue("uom",uom);
            serviceDataHolder.getInputDataAsMap().setAttribValue("qty",qty);

            ServiceHelper.executeService( "NSPRFTCC01", serviceDataHolder);
//            Task.setAppContext("RFRETURNCONTEXT",null);

            //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ttm,
            //taskdetailkey,storerkey,sku,lot,loc,chkdigit,id,qty,packkey,uom,itrnkey,transactionkey
//            if (context.theSQLMgr.isActive()) context.theSQLMgr.transactionCommit();
//            context.theEXEDataObjectStack.pop();
//            EXEDataObject ccInfo = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);

            ServiceDataMap newDo = new ServiceDataMap();

            String ccKey = serviceDataHolder.getInputDataAsMap().getString("CCKEY");
            String ccDetailKey = serviceDataHolder.getInputDataAsMap().getString("CCDETAILKEY");
            newDo.setAttribValue("taskdetailkey",taskDetailKey);
            newDo.setAttribValue("storerkey",storerKey);
            newDo.setAttribValue("sku",sku);
            newDo.setAttribValue("lot",lot);
            newDo.setAttribValue("loc",loc);
            newDo.setAttribValue("id",id);
            newDo.setAttribValue("packKey",packKey);
            newDo.setAttribValue("uom",uom);
            newDo.setAttribValue("qty",qty);
            ServiceHelper.executeService("NSPRFTCC02", serviceDataHolder);

            DBHelper.executeUpdate("update cc set status ='9' where cckey =? ",new Object[]{
                    ccKey
            });
            DBHelper.executeUpdate("update ccdetail set status ='9' where cckey =? ",new Object[]{
                    ccKey
            });

        }catch (Exception e) {
            ExceptionHelper.throwRfFulfillLogicException("发生错误:"+e.getMessage());
        }finally {
            
        }
    }

}
