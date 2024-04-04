package com.enhantec.wms.ui.common.service.impl;

import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.wms.ui.common.model.SkuModel;
import com.enhantec.wms.ui.common.service.SkuService;
import com.enhantec.wms.ui.common.mapper.SkuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
* @author johnw
* @description 针对表【SKU】的数据库操作Service实现
* @createDate 2024-04-03 17:30:39
*/
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@Service
public class SkuServiceImpl extends EHBaseServiceImpl<SkuMapper, SkuModel>
    implements SkuService {

}




