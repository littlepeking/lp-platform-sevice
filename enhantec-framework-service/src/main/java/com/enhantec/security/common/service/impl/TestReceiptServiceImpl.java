package com.enhantec.security.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.TestReceiptService;
import com.enhantec.security.common.mapper.TestReceiptMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Service实现
* @createDate 2022-04-18 17:37:15
*/
@Service
public class TestReceiptServiceImpl extends ServiceImpl<TestReceiptMapper, TestReceipt>
    implements TestReceiptService{


    public List<TestReceipt> findReceiptByReceiptId(String id){

       return getBaseMapper().selectAllById(id);
    }

    public Page<Map<String,Object>> getReceiptPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        return getBaseMapper().selectByReceiptKey(page, qw);
    }


}




