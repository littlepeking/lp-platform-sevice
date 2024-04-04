/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.backend.core.outbound;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.core.WMSCoreServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.framework.WMSBaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service(WMSCoreServiceNames.OUTBOUND_SINGLE_LOT_ID_PICK)
@AllArgsConstructor
public class SingleLotLpnPickService extends WMSBaseService {

    private final OutboundOperations outboundOperations;


    /**
     * 拣货：移动的数量为拣货量，移动的同时更新分配和拣货量。
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

       // String fromId = serviceDataHolder.getInputDataAsMap().getString("fromId");
        //toId为空：当为整容器移动时代表目标容器仍为原容器，当对容器中的部分数量做移动时提示错误，子容器直接传入toId，这里为核心逻，不做LPN生成。

        String pickDetailKey = serviceDataHolder.getInputDataAsMap().getString("pickDetailKey");//拣货明细号

        String toId = serviceDataHolder.getInputDataAsMap().getString("toId");
        String toLoc = serviceDataHolder.getInputDataAsMap().getString("toLoc");
        BigDecimal uomQtyToBePicked = serviceDataHolder.getInputDataAsMap().getDecimalValue("uomQtyToBePicked");
        String uom = serviceDataHolder.getInputDataAsMap().getString("uom");

        boolean allowOverPick = serviceDataHolder.getInputDataAsMap().getBoolean("allowOverPick");//是否允许超拣
        boolean allowShortPick = serviceDataHolder.getInputDataAsMap().getBoolean("allowShortPick");//是否允许短拣
        boolean reduceOpenQtyAfterShortPick = serviceDataHolder.getInputDataAsMap().getBoolean("reduceOpenQtyAfterShortPick");//短拣后是否自动减少需求量


        //if(UtilHelper.isEmpty(fromId)) throw new FulfillLogicException("待拣货的容器号不能为空");

        //Map<String,String> fromIdHashMap = LotxLocxId.findWithoutCheckIDNotes(fromId,true);

//        String sku = fromIdHashMap.get("SKU");
//        BigDecimal qtyInFromId = new BigDecimal(fromIdHashMap.get("QTY"));
//        BigDecimal qtyAvailableInFromId = new BigDecimal(fromIdHashMap.get("AVAILABLEQTY"));
//        BigDecimal qtyAllocatedInFromId = new BigDecimal(fromIdHashMap.get("QTYALLOCATED"));
//        BigDecimal qtyPickedInFromId = new BigDecimal(fromIdHashMap.get("QTYPICKED"));
//        String fromLoc = fromIdHashMap.get("LOC");
//        String lot = fromIdHashMap.get("LOT");


        Map<String, String> pickDetailHashMap = PickDetail.findByPickDetailKey(pickDetailKey,true);
        Map<String, Object> pdIdHashMap = LotxLocxId.findRawRecordWithoutCheckIDNotes(pickDetailHashMap.get("ID"),true);

        if(!pickDetailHashMap.get("STATUS").equals("0") && !pickDetailHashMap.get("STATUS").equals("1")){
            throw new EHApplicationException("当前拣货明细状态不允许拣货");
        }


        //暂不支持扫描容器直接换托盘功能
        //if(!pickDetailHashMap.get("ID").equals(fromId)) throw new EHApplicationException("暂不支持扫描容器直接换托盘功能");
        //if(!pickDetailHashMap.get("LOC").equals(fromLoc)) throw new EHApplicationException("扫描的库位和拣货明细中提供的不一致");

        ////////////////////////////////////////////////////////////////////////////////////////////
        //TODO 如FROMID和拣货明细不一致则直接换托盘拣货
        //
        // if(!pickDetailHashMap.get("ID").equals(fromId))
        // {
        //     if(!pdIdHashMap.get("LOT").equals(fromIdHashMap.get("LOT"))) {
        //         throw new EHApplicationException("拣货明细中的容器号" + pickDetailHashMap.get("ID") + "和当前拣货的容器" + fromId + "的批次不相同，不允许替换");
        //     }
        // }
        ////////////////////////////////////////////////////////////////////////////////////////////

        //调用标准拣货逻辑
        ServiceDataMap serviceDataMap = outboundOperations.pick(pickDetailKey,toId,toLoc,uomQtyToBePicked, uom, allowShortPick, reduceOpenQtyAfterShortPick, allowOverPick);

        serviceDataHolder.setOutputData(serviceDataMap);

    }
}
