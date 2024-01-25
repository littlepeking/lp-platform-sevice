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

package com.enhantec.wms.backend.core.outbound.allocations;

import com.enhantec.wms.backend.core.outbound.OutboundOperations;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import static com.enhantec.wms.backend.core.WMSServiceNames.OUTBOUND_ALLOCATE_ORDER;


@Service(OUTBOUND_ALLOCATE_ORDER)
@AllArgsConstructor
public class AllocationService extends WMSBaseService {

    OutboundOperations outboundOperations;

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
        String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");

        outboundOperations.allocate(orderKey,orderLineNumber);
    }
}
