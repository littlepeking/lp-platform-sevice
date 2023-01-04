package com.enhantec.framework.scheduler.common.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.common.utils.DBConst;
import com.enhantec.framework.scheduler.common.model.EHJobDefinition;
import com.enhantec.framework.scheduler.common.model.EHJobSchedule;
import com.enhantec.framework.scheduler.common.service.EHJobDefinitionService;
import com.enhantec.framework.scheduler.common.mapper.EHJobDefinitionMapper;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
* @author johnw
* @description 针对表【eh_job】的数据库操作Service实现
* @createDate 2022-12-24 00:09:33
*/
@Service
@DS(DBConst.DS_MASTER)
@Transactional(rollbackFor = Exception.class)
public class EHJobDefinitionDefinitionServiceImpl extends EHBaseServiceImpl<EHJobDefinitionMapper, EHJobDefinition>
    implements EHJobDefinitionService {

    EHJobScheduleService jobScheduleService;

    @Override
    public boolean removeById(Serializable id) {

        long jobScheduleCount = jobScheduleService.count(Wrappers.lambdaQuery(EHJobSchedule.class).eq(EHJobSchedule::getJobDefId,id));

        if(jobScheduleCount > 0){
            throw new EHApplicationException("s-job-deleteScheduleBeforeDeleteJob");
        } else {
            return super.removeById(id);
        }
    }


}




