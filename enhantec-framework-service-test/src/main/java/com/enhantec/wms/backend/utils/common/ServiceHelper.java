package com.enhantec.wms.backend.utils.common;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@DS(DSConstants.DS_DEFAULT)
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
public class ServiceHelper {

    public static ServiceDataMap executeService(String serviceName, ServiceDataHolder serviceDataHolder) {

            WMSBaseService service = EHContextHelper.getBean(serviceName, WMSBaseService.class);

            service.execute(serviceDataHolder);

            return serviceDataHolder.getOutputDataAsMap();

    }

    public ServiceDataMap executeService2(String serviceName, ServiceDataHolder serviceDataHolder) {

        WMSBaseService service = EHContextHelper.getBean(serviceName, WMSBaseService.class);

        service.execute(serviceDataHolder);

        return serviceDataHolder.getOutputDataAsMap();

    }
}
