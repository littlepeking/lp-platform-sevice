package com.enhantec.wms.ui.common.service.impl;

import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.wms.ui.common.model.StorerModel;
import com.enhantec.wms.ui.common.service.StorerService;
import com.enhantec.wms.ui.common.mapper.StorerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
* @author johnw
* @description 针对表【STORER】的数据库操作Service实现
* @createDate 2024-04-04 19:21:00
*/

@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@Service
public class StorerServiceImpl extends EHBaseServiceImpl<StorerMapper, StorerModel>
    implements StorerService{

}




