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



package com.enhantec.demo.ui.inbound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHLocaleHelper;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.demo.ui.inbound.model.TestReceipt;
import com.enhantec.demo.ui.inbound.service.TestReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-receipt")
@RequiredArgsConstructor
public class TestReceiptController {

    private final TestReceiptService testReceiptService;

    private final MessageSource messageSource;

    @GetMapping("/search/{id}")
    public List<TestReceipt> findTestReceipt(@PathVariable String id){

        List<TestReceipt>  res = testReceiptService.findReceiptByReceiptId(id);
        return res;

    }

    @GetMapping("translate/{msgKey}")
    public String getI18nMsg(@PathVariable String msgKey){
        return EHLocaleHelper.getMsg(msgKey);
    }

    @GetMapping("translate/test")
    public String testI18nArgs(){
        return EHLocaleHelper.getMsg("c-testArgs","arg1");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/queryByPage")
    public Page<Map<String,Object>> findTestReceiptByReceiptKey(@RequestBody PageParams pageParams){

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String,Object>> res = testReceiptService.getReceiptPageData(pageInfo,queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}
