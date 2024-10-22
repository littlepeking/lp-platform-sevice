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



package com.enhantec.wms.inbound.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.wms.inbound.model.TestReceipt;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Mapper
* @createDate 2022-04-18 17:37:15
* @Entity com.enhantec.wms.inbound.model.TestReceipt.TestReceipt
*/
@Repository
public interface TestReceiptMapper extends EHBaseMapper<TestReceipt> {

    List<TestReceipt> selectAllById(@Param("id") String id);

    @MapKey("id")
    Page<Map<String,Object>> selectByReceiptKey(Page<Map<String,Object>> page, QueryWrapper ew);

}




