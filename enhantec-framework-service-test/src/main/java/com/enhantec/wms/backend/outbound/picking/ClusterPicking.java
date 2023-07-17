package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.task.TaskDetail;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;

public class ClusterPicking extends LegacyBaseService {

    /**
     * JOHN 20201115
     *
     --注册方法

     DELETE FROM SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME= 'EHClusterPicking';
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHClusterPicking', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'ClusterPicking', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,area,sequence,orderkey,extrawhereconditions,extraorderbyconditions','0.10','0');

     */


    //TMEVCP02P1S1 paramters:
    //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ttM,area,sequence,continue,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,arg17,arg18,arg19,arg20,arg21,arg22,arg23,arg24,arg25,arg26,arg27,arg28,arg29,arg30,ioflag,taskkey
    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        
        String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderkey");


        HashMap<String, String>  orderInfo = Orders.findByOrderKey(orderKey,true);

        //if (!"2".equals(orderInfo.get("ISCONFIRMED"))) ExceptionHelper.throwRfFulfillLogicException("订单未复核不允许拣货");

        serviceDataHolder.getInputDataAsMap().setAttribValue("ioflag","1");
        serviceDataHolder.getInputDataAsMap().setAttribValue("continue","1");
        serviceDataHolder.getInputDataAsMap().setAttribValue("arg1",orderKey);
        for(int i=2;i<=30;i++){
            serviceDataHolder.getInputDataAsMap().setAttribValue("arg"+i,"");
        }

        HashMap outDO = null;

        //
        String sku =  outDO.get("sku").toString();
        String lot =  outDO.get("lot").toString();
        String fromId =  outDO.get("fromid").toString();
        String taskDetailKey =  outDO.get("taskdetailkey").toString();



        HashMap<String,String> idNotesInfo = IDNotes.findById(fromId,true);

        HashMap<String,String>  skuHashMap = SKU.findById( sku,true);
        String  stdUom = UOM.getStdUOM( idNotesInfo.get("PACKKEY"));
        HashMap<String,Object>  lotHashMap = VLotAttribute.findByLot( lot,true);
        HashMap<String,String>  idNotesHashMap = IDNotes.findById( fromId,true);
        HashMap<String,String>  taskDetailHashMap = TaskDetail.findById( taskDetailKey,true);
        HashMap<String,String>  lotxLocxIdHashMap = LotxLocxId.findById( fromId,true);
        HashMap<String,String>  orderDetailHashMap = Orders.findOrderDetailByKey(taskDetailHashMap.get("ORDERKEY"),taskDetailHashMap.get("ORDERLINENUMBER"), true);

        outDO.put("skudescr",skuHashMap.get("DESCR"));
        outDO.put("lottable06",lotHashMap.get("LOTTABLE06").toString());
        outDO.put("elottable07",lotHashMap.get("ELOTTABLE07")==null?"":lotHashMap.get("ELOTTABLE07").toString());
        outDO.put("elottable03",lotHashMap.get("ELOTTABLE03")==null?"":lotHashMap.get("ELOTTABLE03").toString());
        outDO.put("barreldescr",idNotesHashMap.get("BARRELDESCR"));
       // outDO.setAttribValue("lpnqty",lotxLocxIdHashMap.get("QTY"));
        outDO.put("status",taskDetailHashMap.get("STATUS"));
        outDO.put("packKey",idNotesInfo.get("PACKKEY"));
        outDO.put("stdUom",stdUom);
        outDO.put("availQty",lotxLocxIdHashMap.get("AVAILABLEQTY"));

        if(!UtilHelper.isEmpty(orderDetailHashMap.get("SERIALNUMBER"))) outDO.put("serialnumber",orderDetailHashMap.get("SERIALNUMBER"));
        outDO.put("grosswgt",idNotesInfo.get("GROSSWGT"));
        outDO.put("netwgt",idNotesInfo.get("NETWGT"));
        outDO.put("tarewgt",idNotesInfo.get("TAREWGT"));

        serviceDataHolder.setOutputData(outDO);

    }

}
