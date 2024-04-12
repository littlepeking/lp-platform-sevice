package com.enhantec.wms.ui.inbound.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.wms.ui.inbound.model.ReceiptDetailModel;

import java.util.Collection;
import java.util.Map;

/**
* @author johnw
* @description 针对表【RECEIPTDETAIL】的数据库操作Service
* @createDate 2024-04-03 09:59:07
*/
public interface ReceiptDetailService extends EHBaseService<ReceiptDetailModel> {

    boolean deleteByIds(Collection<String> list);
}
