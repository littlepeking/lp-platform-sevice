package com.enhantec.wms.backend.utils.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
public class ServiceHelper {
    @DS(DSConstants.DS_DEFAULT)
    public static ServiceDataMap executeService(String serviceName, ServiceDataHolder serviceDataHolder) {

            LegacyBaseService service = EHContextHelper.getBean(serviceName, LegacyBaseService.class);

            service.execute(serviceDataHolder);

            return serviceDataHolder.getOutputDataAsMap();

    }
}
