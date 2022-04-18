package com.enhantec.security.common.service;

import com.enhantec.security.common.model.TestReceipt;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Service
* @createDate 2022-04-18 17:37:15
*/
public interface TestReceiptService extends IService<TestReceipt> {

    public List<TestReceipt> getReceiptById(String id);

}
