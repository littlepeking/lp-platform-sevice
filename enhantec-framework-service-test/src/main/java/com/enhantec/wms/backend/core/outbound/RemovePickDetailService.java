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
import com.enhantec.wms.backend.core.WMSOperations;
import com.enhantec.wms.backend.core.WMSServiceNames;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service(WMSServiceNames.OUTBOUND_REMOVE_SINGLE_LOT_ID_PD)
@AllArgsConstructor
public class RemovePickDetailService extends WMSBaseService {

    private final WMSOperations wmsOperations;

    /**
     * 删除拣货明细
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String pickDetailKey = serviceDataHolder.getInputDataAsMap().getString("pickDetailKey");

        if(UtilHelper.isEmpty(pickDetailKey)) throw new EHApplicationException("拣货明细号不能为空");

        ServiceDataMap serviceDataMap = wmsOperations.removePickDetail(pickDetailKey);

        serviceDataHolder.setOutputData(serviceDataMap);
    }
}