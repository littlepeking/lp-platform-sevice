package com.enhantec.security.common.mapper;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.security.common.model.EHUser;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import com.enhantec.security.common.model.TestReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Mapper
* @createDate 2022-04-18 17:37:15
* @Entity com.enhantec.security.common.model.TestReceipt
*/
public interface TestReceiptMapper extends EHBaseMapper<TestReceipt> {

    List<TestReceipt> selectAllById(@Param("id") String id);

    @MapKey("id")
    Page<Map<String,Object>> selectByReceiptKey(Page<Map<String,Object>> page, QueryWrapper ew);

}




