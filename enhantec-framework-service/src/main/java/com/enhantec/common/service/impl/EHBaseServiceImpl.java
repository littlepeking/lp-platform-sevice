package com.enhantec.common.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.common.service.EHBaseService;

public class EHBaseServiceImpl< M extends BaseMapper<T>, T extends EHBaseModel> extends ServiceImpl<M, T  > implements EHBaseService<T> {

    //To resolve original saveOrUpdate method return partial entity with updated columns only.
    public T saveOrUpdateAndRetE(T model){

        saveOrUpdate(model);

        return baseMapper.selectById(model.getId());

    }
}
