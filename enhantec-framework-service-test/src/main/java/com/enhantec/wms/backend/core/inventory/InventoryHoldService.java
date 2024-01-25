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
import com.enhantec.wms.backend.core.WMSServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service(WMSServiceNames.INV_SINGLE_LOT_ID_MOVE)
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class InventoryHoldService extends WMSBaseService {

    private final InventoryOperations inventoryOperations;

    public void execute(ServiceDataHolder serviceDataHolder) {


        String id = serviceDataHolder.getInputDataAsMap().getString("id");
        String lot = serviceDataHolder.getInputDataAsMap().getString("lot");
        String loc = serviceDataHolder.getInputDataAsMap().getString("loc");
        String reasonCode = serviceDataHolder.getInputDataAsMap().getString("reasonCode");

        //HOLD / UNHOLD / DELETE = UNHOLD + DELETE INVENTORY HOLD RECORD
        String operationType = serviceDataHolder.getInputDataAsMap().getString("operationType");

        if (((UtilHelper.isEmpty(id) ? 1 : 0) + (UtilHelper.isEmpty(lot) ? 1 : 0) + (UtilHelper.isEmpty(loc) ? 1 : 0)) != 1) {
            throw new EHApplicationException("容器ID、批次或库位参数必须传且只允许传入一个");
        }

        if(UtilHelper.isEmpty(reasonCode)) throw new EHApplicationException("冻结原因代码不能为空");
        if(UtilHelper.isEmpty(operationType)) throw new EHApplicationException("操作类型不能为空");

        if("HOLD".equals(operationType)) {

            if(!UtilHelper.isEmpty(id)) inventoryOperations.holdById(id, reasonCode);
            if(!UtilHelper.isEmpty(lot)) inventoryOperations.holdByLot(lot, reasonCode);
            if(!UtilHelper.isEmpty(loc)) inventoryOperations.holdByLoc(loc, reasonCode);

        }else if("UNHOLD".equals(operationType)) {

            if(!UtilHelper.isEmpty(id)) inventoryOperations.unholdById(id, reasonCode, false);
            if(!UtilHelper.isEmpty(lot)) inventoryOperations.unholdByLot(lot, reasonCode, false);
            if(!UtilHelper.isEmpty(loc)) inventoryOperations.unholdByLoc(loc, reasonCode, false);

        }else if("DELETE".equals(operationType)) {

            if(!UtilHelper.isEmpty(id)) inventoryOperations.unholdById(id, reasonCode, true);
            if(!UtilHelper.isEmpty(lot)) inventoryOperations.unholdByLot(lot, reasonCode, true);
            if(!UtilHelper.isEmpty(loc)) inventoryOperations.unholdByLoc(loc, reasonCode, true);

        }else {
            throw new EHApplicationException("不支持该冻结操作:" + operationType);
        }

    }
}
