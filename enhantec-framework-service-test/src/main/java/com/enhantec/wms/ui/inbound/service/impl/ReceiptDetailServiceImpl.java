package com.enhantec.wms.ui.inbound.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.wms.ui.inbound.model.ReceiptDetailModel;
import com.enhantec.wms.ui.inbound.service.ReceiptDetailService;
import com.enhantec.wms.ui.inbound.mapper.ReceiptDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
* @author johnw
* @description 针对表【RECEIPTDETAIL】的数据库操作Service实现
* @createDate 2024-04-03 09:59:07
*/

@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@Service
@DS(DSConstants.DS_DEFAULT)
public class ReceiptDetailServiceImpl extends EHBaseServiceImpl<ReceiptDetailMapper, ReceiptDetailModel>
    implements ReceiptDetailService {

}




