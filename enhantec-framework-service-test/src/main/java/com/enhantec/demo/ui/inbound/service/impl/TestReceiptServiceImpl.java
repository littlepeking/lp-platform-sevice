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



package com.enhantec.demo.ui.inbound.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.demo.ui.inbound.mapper.TestReceiptMapper;
import com.enhantec.demo.ui.inbound.model.TestReceipt;
import com.enhantec.demo.ui.inbound.service.TestReceiptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Service实现
* @createDate 2022-04-18 17:37:15
*/
@Service
@DS(DSConstants.DS_DEFAULT)
public class TestReceiptServiceImpl extends ServiceImpl<TestReceiptMapper, TestReceipt>
    implements TestReceiptService {


    public List<TestReceipt> findReceiptByReceiptId(String id){

       return getBaseMapper().selectAllById(id);
    }

    //@DS(DBConst.DS_CONTEXT_ORG)
    public Page<Map<String,Object>> getReceiptPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        return getBaseMapper().selectByReceiptKey(page, qw);
    }


}




