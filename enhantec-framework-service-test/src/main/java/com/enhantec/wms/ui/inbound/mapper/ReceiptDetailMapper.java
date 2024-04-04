package com.enhantec.wms.ui.inbound.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.wms.ui.inbound.model.ReceiptDetailModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;

import java.util.Map;

/**
* @author johnw
* @description 针对表【RECEIPTDETAIL】的数据库操作Mapper
* @createDate 2024-04-03 09:59:07
* @Entity com.enhantec.wms.ui.inbound.ReceiptDetailModel
*/
public interface ReceiptDetailMapper extends EHBaseMapper<ReceiptDetailModel> {

    Page<Map<String,Object>> queryPageData(Page<Map<String,Object>> page, QueryWrapper ew);

}




