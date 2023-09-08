package com.enhantec.wms.backend.framework;

import com.enhantec.wms.backend.utils.common.ServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wms-services")
@RequiredArgsConstructor
public class LegacyWMSServiceController {

    private  final ServiceHelper serviceHelper;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{serviceName}")
    public Object execute(@NotNull @PathVariable String serviceName, @RequestBody HashMap params) {

        //todo: set permission for each service, can add annotation on concrete service class. Here to check if permission allowed in given orgId.

        ServiceDataHolder serviceDataHolder = new ServiceDataHolder(new ServiceDataMap(params));

        serviceHelper.executeService2(serviceName, serviceDataHolder);

        return serviceDataHolder.getOutputDataAsMap()==null ? null : serviceDataHolder.getOutputDataAsMap().getData();
    }



}


