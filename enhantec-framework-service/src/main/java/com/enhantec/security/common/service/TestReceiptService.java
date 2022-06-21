package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.security.common.model.TestReceipt;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Service
* @createDate 2022-04-18 17:37:15
*/
public interface TestReceiptService extends IService<TestReceipt> {

    public List<TestReceipt> findReceiptByReceiptId(String id);

    public Page<Map<String,Object>> getReceiptPageData(Page<Map<String,Object>> page, QueryWrapper qw);

}
