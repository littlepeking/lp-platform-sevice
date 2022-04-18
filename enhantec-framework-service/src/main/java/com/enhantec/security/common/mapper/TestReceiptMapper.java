package com.enhantec.security.common.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.enhantec.security.common.model.TestReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author johnw
* @description 针对表【test_receipt】的数据库操作Mapper
* @createDate 2022-04-18 17:37:15
* @Entity com.enhantec.security.common.model.TestReceipt
*/
public interface TestReceiptMapper extends BaseMapper<TestReceipt> {


    List<TestReceipt> selectAllById(@Param("id") String id);

}




