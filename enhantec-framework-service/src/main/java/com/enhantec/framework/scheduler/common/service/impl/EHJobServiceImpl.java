package com.enhantec.framework.scheduler.common.service.impl;

import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.scheduler.common.model.EHJobModel;
import com.enhantec.framework.scheduler.common.service.EHJobService;
import com.enhantec.framework.scheduler.common.mapper.EHJobMapper;
import org.springframework.stereotype.Service;

/**
* @author johnw
* @description 针对表【eh_job】的数据库操作Service实现
* @createDate 2022-12-24 00:09:33
*/
@Service
public class EHJobServiceImpl extends EHBaseServiceImpl<EHJobMapper, EHJobModel>
    implements EHJobService{

}




