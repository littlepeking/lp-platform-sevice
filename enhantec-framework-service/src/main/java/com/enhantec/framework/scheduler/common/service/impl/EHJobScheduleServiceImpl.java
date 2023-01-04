package com.enhantec.framework.scheduler.common.service.impl;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.framework.scheduler.common.model.EHJobSchedule;
import com.enhantec.framework.scheduler.common.service.EHJobScheduleService;
import com.enhantec.framework.scheduler.common.mapper.EHJobScheduleMapper;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
* @author johnw
* @description 针对表【eh_job_schedule】的数据库操作Service实现
* @createDate 2022-12-24 12:56:49
*/
@Service
public class EHJobScheduleServiceImpl extends EHBaseServiceImpl<EHJobScheduleMapper, EHJobSchedule>
    implements EHJobScheduleService{

    @Override
    public boolean removeById(Serializable id) {

         EHJobSchedule jobSchedule = getById(id);

         if(jobSchedule.isEnabled()){
             throw new EHApplicationException("s-schedule-stopScheduleBeforeDelete");
         } else {
            return super.removeById(id);
         }
    }


}




