package com.enhantec.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.model.EHBaseModel;

public interface EHBaseService<T extends EHBaseModel> extends IService<T> {

    T saveOrUpdateAndRetE(T model);

    /**
     * Currently we only implement this updateById method support optimisticLock throw Exception.
     * @param entity
     * @return
     */
    default boolean updateById(T entity) {
        boolean isUpdateSuccess = SqlHelper.retBool(this.getBaseMapper().updateById(entity));
        if(!isUpdateSuccess) throw new EHApplicationException("c-sys-dataChangedByOtherUser");

        return true;
    }
}
