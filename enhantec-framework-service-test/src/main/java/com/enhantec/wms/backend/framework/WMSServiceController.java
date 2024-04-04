package com.enhantec.wms.backend.framework;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

import static com.enhantec.wms.Constants.WMS_APP_SERVICE;

@RestController
@RequestMapping(WMS_APP_SERVICE)
@RequiredArgsConstructor
public class WMSServiceController {

    private  final WMSCommonService wmsCommonService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{serviceName}")
    public Object execute(@NotNull @PathVariable String serviceName, @RequestBody HashMap params) {

        //todo: set permission for each service, can add annotation on concrete service class. Here to check if permission allowed in given orgId.

        ServiceDataHolder serviceDataHolder = new ServiceDataHolder(new ServiceDataMap(params));

        wmsCommonService.execute(serviceName, serviceDataHolder);

        return serviceDataHolder.getOutputDataAsMap()==null ? null : serviceDataHolder.getOutputDataAsMap().getData();
    }



}


