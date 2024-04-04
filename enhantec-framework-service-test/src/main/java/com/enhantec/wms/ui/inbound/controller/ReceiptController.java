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



package com.enhantec.wms.ui.inbound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.wms.ui.inbound.model.ReceiptModel;
import com.enhantec.wms.ui.inbound.service.ReceiptDetailService;
import com.enhantec.wms.ui.inbound.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wms/inbound/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;


    private final ReceiptDetailService receiptDetailService;


    @GetMapping("/findById/{id}")
    public ReceiptModel findById(@PathVariable String id){
        return receiptService.getById(id);

    }

    @PreAuthorize("hasAnyAuthority('WM_ASN')")
    @PostMapping("/queryReceiptByPage")
    public Page<Map<String, Object>> queryReceiptByPage(@RequestBody PageParams pageParams) {

        return receiptService.queryPageData(pageParams,EHFieldNameConversionType.NONE);

    }


    @PreAuthorize("hasAnyAuthority('WM_ASN')")
    @PostMapping("/queryReceiptDetailByPage")
    public Page<Map<String, Object>> queryReceiptDetailByPage(@RequestBody PageParams pageParams) {

        return receiptDetailService.queryPageData(pageParams,EHFieldNameConversionType.NONE);

    }



}
