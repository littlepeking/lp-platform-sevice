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

package com.enhantec.wms.backend.framework;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.common.utils.EHContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DS(DSConstants.DS_DEFAULT)
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class WMSCommonService {

        public ServiceDataMap execute(String serviceName, ServiceDataHolder serviceDataHolder){

            WMSBaseService service = EHContextHelper.getBean(serviceName, WMSBaseService.class);

            service.execute(serviceDataHolder);

            return serviceDataHolder.getOutputDataAsMap();

        }
}
