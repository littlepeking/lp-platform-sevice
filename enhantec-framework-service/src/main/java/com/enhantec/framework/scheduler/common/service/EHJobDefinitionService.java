package com.enhantec.framework.scheduler.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.common.utils.DBConst;
import com.enhantec.framework.scheduler.common.model.EHJobDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


public interface EHJobDefinitionService extends EHBaseService<EHJobDefinition> {

}
