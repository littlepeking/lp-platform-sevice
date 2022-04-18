package com.enhantec.security.common.controller;

import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.TestReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/system/test-receipt")
@RequiredArgsConstructor
public class TestReceiptController {

    private final TestReceiptService testReceiptService;

    @GetMapping("/search/{id}")
    public List<TestReceipt> getTestReceipt(@PathVariable String id){

        List<TestReceipt>  res = testReceiptService.getReceiptById(id);
        return res;

    }

}
