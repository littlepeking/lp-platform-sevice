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

import com.enhantec.wms.backend.core.WMSServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service(WMSServiceNames.INV_INTERNAL_TRANSFER)
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class InternalTransferService extends WMSBaseService {

    private final InventoryOperations inventoryOperations;

    public void execute(ServiceDataHolder serviceDataHolder) {


        String id = serviceDataHolder.getInputDataAsMap().getString("id");
        String lot = serviceDataHolder.getInputDataAsMap().getString("lot");
        HashMap overrideLotAttributeList = (HashMap) serviceDataHolder.getInputDataAsMap().getAttribValue("overrideLotAttributeList");


        inventoryOperations.internalTransfer(id, lot,overrideLotAttributeList);


    }
}
