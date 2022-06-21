package com.enhantec.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.common.model.EHBaseModel;

public interface EHBaseService<T extends EHBaseModel> extends IService<T> {

    T saveOrUpdateAndRetE(T model);
}
