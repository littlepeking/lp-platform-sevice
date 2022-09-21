package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHLocaleHelper;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.TestReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
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


    @PostMapping("/queryByPage")
    public Page<Map<String,Object>> findTestReceiptByReceiptKey(@RequestBody PageParams pageParams){

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String,Object>> res = testReceiptService.getReceiptPageData(pageInfo,queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}
