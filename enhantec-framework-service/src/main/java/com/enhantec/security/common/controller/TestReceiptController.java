package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.TestReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/api/test-receipt")
@RequiredArgsConstructor
public class TestReceiptController {

    private final TestReceiptService testReceiptService;

    @GetMapping("/search/{id}")
    public List<TestReceipt> getTestReceipt(@PathVariable String id){

        List<TestReceipt>  res = testReceiptService.getReceiptByReceiptId(id);
        return res;

    }

    @PostMapping("/queryByPage")
    public Page<Map<String,Object>> getTestReceiptByReceiptKey(@RequestBody PageParams pageParams){

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String,Object>> res = testReceiptService.getReceiptPageData(pageInfo,queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}