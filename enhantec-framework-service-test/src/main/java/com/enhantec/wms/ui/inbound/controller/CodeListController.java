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
import com.enhantec.wms.ui.common.service.CodeLkupService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wms/common/code-list")
@RequiredArgsConstructor
public class CodeListController {

//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/queryByPage")
//    public Page<Map<String, Object>> queryByPage(@RequestBody PageParams pageParams) {
//
//        Page pageInfo = EHPaginationHelper.buildPageInfo(pageParams);
//
//        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams, EHFieldNameConversionType.NONE);
//
//        Page<Map<String, Object>> res = receiptService.queryPageData(pageInfo, queryWrapper);
//
//        return res;
//    }
//


}