package com.enhantec.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.common.service.EHBaseService;
import com.enhantec.common.utils.EHTranslationHelper;
import org.springframework.transaction.annotation.Transactional;

public class EHBaseServiceImpl<M extends EHBaseMapper<T>, T extends EHBaseModel> extends ServiceImpl<M, T> implements EHBaseService<T> {

    //To resolve original saveOrUpdate method return partial entity with updated columns only.
    @Transactional(rollbackFor = Exception.class)
    public T saveOrUpdateTr(T model) {

        super.saveOrUpdate(model);

        EHTranslationHelper.saveTranslation(model);

        return baseMapper.selectByIdTr(model.getId());

    }

    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(T model) {
        throw new RuntimeException("Please replace method saveOrUpdate by saveOrUpdateTr.");
    }

}
