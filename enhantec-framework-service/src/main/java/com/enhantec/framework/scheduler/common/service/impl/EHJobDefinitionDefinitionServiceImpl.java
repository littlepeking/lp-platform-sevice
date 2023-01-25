package com.enhantec.framework.scheduler.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DBConst;
import com.enhantec.framework.scheduler.common.model.EHJobDefinitionModel;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.common.mapper.EHJobDefinitionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author johnw
* @description 针对表【eh_job】的数据库操作Service实现
* @createDate 2022-12-24 00:09:33
*/
@Service
@DS(DBConst.DS_MASTER)
@Transactional(rollbackFor = Exception.class)
public class EHJobDefinitionDefinitionServiceImpl extends EHBaseServiceImpl<EHJobDefinitionMapper, EHJobDefinitionModel>
    implements EHJobDefinitionService {

}




