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
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.wms.ui.inbound.model.ReceiptDetailModel;
import com.enhantec.wms.ui.inbound.service.ReceiptDetailService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

import static com.enhantec.wms.Constants.WMS_UI_SERVICE;

@RestController
@RequestMapping(WMS_UI_SERVICE+"/inbound/receiptDetail")
@RequiredArgsConstructor
public class ReceiptDetailController {


    private final ReceiptDetailService receiptDetailService;


    @GetMapping("/findById/{id}")
    public ReceiptDetailModel findById(@PathVariable String id){
        return receiptDetailService.getById(id);

    }

    @DeleteMapping("/deleteByIds")
    public boolean deleteByIds(@RequestBody List<String> ids){
        return receiptDetailService.deleteByIds(ids);
    }




    @PreAuthorize("hasAnyAuthority('WM_ASN')")
    @PostMapping("/queryByPage")
    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {

        return receiptDetailService.queryPageData(pageParams,EHFieldNameConversionType.NONE);

    }



}