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



package com.enhantec.wms.ui.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHTranslationHelper;
import com.enhantec.framework.config.TransFieldConfig;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.ui.common.service.CodeLkupService;
import com.enhantec.wms.ui.common.service.StorerService;
import com.enhantec.wms.ui.inbound.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.enhantec.wms.Constants.WMS_UI_SERVICE;

@RestController
@RequestMapping(WMS_UI_SERVICE+"/common/storer")
@RequiredArgsConstructor
public class StorerController {

    private final StorerService storerService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        return storerService.queryPageData(pageParams, EHFieldNameConversionType.NONE);

    }


}
