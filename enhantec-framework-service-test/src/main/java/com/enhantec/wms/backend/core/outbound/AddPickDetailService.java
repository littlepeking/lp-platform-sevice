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
import com.enhantec.wms.backend.core.WMSCoreOperations;
import com.enhantec.wms.backend.core.WMSCoreServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service(WMSCoreServiceNames.OUTBOUND_ADD_SINGLE_LOT_ID_PD)
@AllArgsConstructor
public class AddPickDetailService extends WMSBaseService {

    private final WMSCoreOperations wmsCoreOperations;

    /**
     * 添加拣货明细
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderKey");
        String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("orderLineNumber");
        String id = serviceDataHolder.getInputDataAsMap().getString("id");
        String packKey = serviceDataHolder.getInputDataAsMap().getString("packKey");
        BigDecimal uomQty = serviceDataHolder.getInputDataAsMap().getDecimalValue("uomQty");
        String uom = serviceDataHolder.getInputDataAsMap().getString("uom");


        if(UtilHelper.isEmpty(orderKey)) throw new EHApplicationException("订单号不能为空");
        if(UtilHelper.isEmpty(orderLineNumber)) throw new EHApplicationException("订单行号不能为空");
        if(UtilHelper.isEmpty(packKey)) throw new EHApplicationException("包装不能为空");
        if(UtilHelper.isEmpty(uomQty)) throw new EHApplicationException("数量不能为空");
        if(UtilHelper.isEmpty(uom)) throw new EHApplicationException("单位不能为空");

        Map<String,String> idHashMap = LotxLocxId.findWithoutCheckIDNotes(id,true);

        String lot = idHashMap.get("LOT");

        ServiceDataMap serviceDataMap = wmsCoreOperations.addPickDetail(orderKey, orderLineNumber, lot, id, packKey, uom, uomQty);

        serviceDataHolder.setOutputData(serviceDataMap);
    }
}
