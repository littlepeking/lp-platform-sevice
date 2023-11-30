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

import com.enhantec.wms.backend.core.WMSOperations;
import com.enhantec.wms.backend.core.WMSServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service(WMSServiceNames.OUTBOUND_SHIP_BY_ORDER)
@AllArgsConstructor
public class ShipByOrderService extends WMSBaseService {

    private final WMSOperations wmsOperations;


    /**
     * 根据订单号发运
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderKey");

        wmsOperations.shipByOrder(orderKey,false);

    }
}
