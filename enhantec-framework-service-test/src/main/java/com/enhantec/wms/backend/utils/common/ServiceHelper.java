package com.enhantec.wms.backend.utils.common;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;

public class ServiceHelper {

    public static ServiceDataMap executeService(String serviceName, ServiceDataHolder serviceDataHolder) {

            WMSBaseService service = EHContextHelper.getBean(serviceName, WMSBaseService.class);

            service.execute(serviceDataHolder);

            return serviceDataHolder.getOutputDataAsMap();

    }

}
