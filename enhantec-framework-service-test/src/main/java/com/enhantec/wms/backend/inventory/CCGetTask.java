package com.enhantec.wms.backend.inventory;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.LotAttribute;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

import java.util.Map;

/**
 --??
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHCCGetTask';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHCCGetTask', 'com.enhantec.sce.inventory', 'enhantec', 'CCGetTask','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,area,taskKey','0.10','0');
 **/


public class CCGetTask extends WMSBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TTM,
        // area01,area02,area03,area04,area05,lastloc,lasttask,taskoverride,taskdetailkey
        serviceDataHolder.getInputDataAsMap().setAttribValue("area01", serviceDataHolder.getInputDataAsMap().getString("area"));
        serviceDataHolder.getInputDataAsMap().setAttribValue("SOURCEKEY", serviceDataHolder.getInputDataAsMap().getString("taskKey"));
        serviceDataHolder.getInputDataAsMap().setAttribValue("lasttask","CC");
        serviceDataHolder.getInputDataAsMap().setAttribValue("taskoverride","CC");
        ServiceDataMap outDO = (ServiceDataMap) ServiceHelper.executeService("NSPRFTM01", serviceDataHolder);
        //
        String sku =  outDO.getString("sku");
        String lot =  outDO.getString("lot");
        String fromId =  outDO.getString("fromid");
        String taskDetailKey =  outDO.getString("taskdetailkey");

        Map<String,String> skuHashMap = SKU.findById(sku,true);
        String  stdUom = UOM.getStdUOM(skuHashMap.get("PACKKEY"));
        Map<String,Object>  lotHashMap = LotAttribute.findWithEntByLot(lot,true);
        Map<String,String>  idNotesHashMap = IDNotes.findById(fromId,true);
        Map<String,String>  taskDetailHashMap = TaskDetail.findById(taskDetailKey,true);
        Map<String,String>  lotxLocxIdHashMap = LotxLocxId.findById(fromId,true);

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