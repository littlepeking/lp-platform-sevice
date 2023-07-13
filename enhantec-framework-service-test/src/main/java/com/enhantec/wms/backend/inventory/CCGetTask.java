package com.enhantec.wms.backend.inventory;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

import java.util.HashMap;

/**
 --??
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHCCGetTask';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHCCGetTask', 'com.enhantec.sce.inventory', 'enhantec', 'CCGetTask','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,area,taskKey','0.10','0');
 **/


public class CCGetTask extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TTM,
        // area01,area02,area03,area04,area05,lastloc,lasttask,taskoverride,taskdetailkey
        serviceDataHolder.getInputDataAsMap().setAttribValue("area01", serviceDataHolder.getInputDataAsMap().getString("area"));
        serviceDataHolder.getInputDataAsMap().setAttribValue("SOURCEKEY", serviceDataHolder.getInputDataAsMap().getString("taskKey"));
        serviceDataHolder.getInputDataAsMap().setAttribValue("lasttask","CC");
        serviceDataHolder.getInputDataAsMap().setAttribValue("taskoverride","CC");
        ServiceDataMap outDO = (ServiceDataMap) ServiceHelper.executeService(context,"NSPRFTM01", serviceDataHolder);
        //Connection conn = context.getConnection();
        String sku =  outDO.getString("sku");
        String lot =  outDO.getString("lot");
        String fromId =  outDO.getString("fromid");
        String taskDetailKey =  outDO.getString("taskdetailkey");

        HashMap<String,String> skuHashMap = SKU.findById(context, null,sku,true);
        String  stdUom = UOM.getStdUOM(context, null,skuHashMap.get("PACKKEY"));
        HashMap<String,Object>  lotHashMap = VLotAttribute.findByLot(context, null,lot,true);
        HashMap<String,String>  idNotesHashMap = IDNotes.findById(context, null,fromId,true);
        HashMap<String,String>  taskDetailHashMap = TaskDetail.findById(context, null,taskDetailKey,true);
        HashMap<String,String>  lotxLocxIdHashMap = LotxLocxId.findById(context, null,fromId,true);

        outDO.setAttribValue("skudescr",skuHashMap.get("DESCR"));
        outDO.setAttribValue("lottable06",lotHashMap.get("LOTTABLE06").toString());
        outDO.setAttribValue("barreldescr",idNotesHashMap.get("BARRELDESCR"));
        outDO.setAttribValue("netwgt",idNotesHashMap.get("NETWGT"));
        outDO.setAttribValue("tarewgt",idNotesHashMap.get("TAREWGT"));
        outDO.setAttribValue("grosswgt",idNotesHashMap.get("GROSSWGT"));
        //outDO.setAttribValue("lpnqty",lotxLocxIdHashMap.get("QTY"));
        outDO.setAttribValue("status",taskDetailHashMap.get("STATUS"));
        outDO.setAttribValue("packKey",idNotesHashMap.get("PACKKEY"));
        outDO.setAttribValue("stdUom",stdUom);


        serviceDataHolder.setReturnCode(1);
        serviceDataHolder.setOutputData(outDO);

    }

}