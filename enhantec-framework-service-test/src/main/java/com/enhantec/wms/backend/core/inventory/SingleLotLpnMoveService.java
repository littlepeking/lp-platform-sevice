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

package com.enhantec.wms.backend.core.inventory;


import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.core.WMSCoreServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service(WMSCoreServiceNames.INV_SINGLE_LOT_ID_MOVE)
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class SingleLotLpnMoveService extends WMSBaseService {

    private final InventoryOperations inventoryOperations;

    /**
     * 支持的移动方式：
     * 标准移动：只允许移动托盘可用量至目标库位容器。
     * 移动已被分配或部分分配的整托盘：LPN会被移动，同时移动的分配量会被同时更新对应的拣货明细记录。
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String fromId = serviceDataHolder.getInputDataAsMap().getString("fromId");
        //toId为空：当为整容器移动时代表目标容器仍为原容器，当对容器中的部分数量做移动时提示错误，子容器直接传入toId，这里为核心逻辑，不做LPN生成。
        String toId = serviceDataHolder.getInputDataAsMap().getString("toId");
        String toLoc = serviceDataHolder.getInputDataAsMap().getString("toLoc");
        BigDecimal qtyToBeMoved = serviceDataHolder.getInputDataAsMap().getDecimalValue("qty");

        // 默认允许移动带分配量或拣货量的整容器
        Boolean allowMoveAllocOrPickedId = serviceDataHolder.getInputDataAsMap().getBoolean("allowMoveAllocOrPickedId");
        allowMoveAllocOrPickedId =  allowMoveAllocOrPickedId == null ? true : allowMoveAllocOrPickedId ;

        if(UtilHelper.isEmpty(fromId)) throw new FulfillLogicException("移动的源容器号不能为空");

        Map<String,String> fromIdHashMap = LotxLocxId.findWithoutCheckIDNotes(fromId,true);

        if(fromIdHashMap.get("LOC").equals(toLoc) && fromId.equals(toId))
            throw new EHApplicationException("容器已在目标库位，不需要移动");


        String sku = fromIdHashMap.get("SKU");
        BigDecimal qtyInFromId = new BigDecimal(fromIdHashMap.get("QTY"));
        BigDecimal qtyAvailableInFromId = new BigDecimal(fromIdHashMap.get("AVAILABLEQTY"));
        BigDecimal qtyAllocatedInFromId = new BigDecimal(fromIdHashMap.get("QTYALLOCATED"));
        BigDecimal qtyPickedInFromId = new BigDecimal(fromIdHashMap.get("QTYPICKED"));
        String lot = fromIdHashMap.get("LOT");
        String fromLoc = fromIdHashMap.get("LOC");
        if(UtilHelper.isEmpty(toLoc)) toLoc = fromLoc; //用于拆分合并容器的情况

        if(qtyToBeMoved.compareTo(qtyInFromId)>0) throw new EHApplicationException("请求移动数量不能超过容器内总数量");

        boolean isFullLpnMove = qtyToBeMoved.compareTo(qtyInFromId) == 0;

        if(UtilHelper.isEmpty(toId)){
            if(isFullLpnMove){
                toId = fromId;
            }else {
               throw new EHApplicationException("非整容器移动必须提供目标容器号");
            }
        }

        BigDecimal qtyAllocChangeFromId;
        BigDecimal qtyPickChangeFromId;
        BigDecimal qtyAllocChangeToId;
        BigDecimal qtyPickChangeToId;


        //整容器移动支持两种场景：
        //1.在目标容器号等于原容器号的情况下：表示沿用原容器号，无需合并到其他容器，这种情况下，允许任意移动。
        //2.在目标容器号不等于原容器号的情况下：则判断原容器是否存在分配量和拣货量，如不存在则可以移动，否则会导致ID和PICKDETAIL不一致，不允许移动。
        if(qtyAllocatedInFromId.compareTo(BigDecimal.ZERO) != 0 || qtyPickedInFromId.compareTo(BigDecimal.ZERO) != 0){

            if (isFullLpnMove) {
                if (!allowMoveAllocOrPickedId)
                    throw new EHApplicationException("容器" + fromId + "存在分配量或拣货量，不允许移动");
                if (fromId != toId)
                    throw new EHApplicationException("容器" + fromId + "存在分配量或拣货量，不允许更换容器号");

                String fromLocationType = DBHelper.getStringValue("SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new String[]{fromLoc});
                String toLocationType = DBHelper.getStringValue("SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new String[]{toLoc});

                //拣货量和分配量不应该同时出现在一个同容器上，PICKTO库位不应该出现有分配量的容器且普通存储库位不应该存在有拣货量的容器，避免带来逻辑混乱。
                if(fromLocationType.equals("PICKTO") && !toLocationType.equals("PICKTO")||
                        !fromLocationType.equals("PICKTO") && toLocationType.equals("PICKTO")){
                    throw new EHApplicationException("当前容器存在分配量或拣货量，原库位类型和目标库位类型必须保持一致");
                }

                DBHelper.executeUpdate("UPDATE PICKDETAIL SET FROMLOC = ?, LOC = ?, EDITWHO = ?, EDITDATE = ? WHERE STATUS < 5 AND LOT = ? AND LOC = ? AND ID = ? "
                            , new Object[]{toLoc, toLoc, EHContextHelper.getUser().getUsername(), EHDateTimeHelper.getCurrentDate(), lot, fromLoc, fromId});

                qtyAllocChangeFromId = qtyAllocatedInFromId;
                qtyPickChangeFromId = qtyPickedInFromId;
                qtyAllocChangeToId = qtyAllocatedInFromId;
                qtyPickChangeToId = qtyPickedInFromId;

            }else {
                throw new EHApplicationException("容器" + fromId + "存在分配量或拣货量，不允许对部分数量进行移动");
            }

        }else {
            //部分容器数量移动，只允许移动可用库存
            if (qtyToBeMoved.compareTo(qtyAvailableInFromId) > 0)
                throw new EHApplicationException("容器" + fromId + "的可用量数量小于请求的移动数量");

            qtyAllocChangeFromId = BigDecimal.ZERO;
            qtyPickChangeFromId = BigDecimal.ZERO;
            qtyAllocChangeToId = BigDecimal.ZERO;
            qtyPickChangeToId = BigDecimal.ZERO;
        }

        ServiceDataMap serviceDataMap = inventoryOperations.move(fromId,toId,fromLoc,toLoc,lot, lot, qtyToBeMoved,qtyAllocChangeFromId,qtyPickChangeFromId,qtyAllocChangeToId,qtyPickChangeToId,true);

        serviceDataHolder.setOutputData(serviceDataMap);

    }
}
