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

import com.enhantec.wms.backend.core.WMSCoreServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service(WMSCoreServiceNames.OUTBOUND_SHIP_BY_ID)
@AllArgsConstructor
public class ShipByIdService extends WMSBaseService {

    private final OutboundOperations outboundOperations;


    /**
     * 根据容器条码发运
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String id = serviceDataHolder.getInputDataAsMap().getString("id");

        outboundOperations.shipById(id);

    }
}
