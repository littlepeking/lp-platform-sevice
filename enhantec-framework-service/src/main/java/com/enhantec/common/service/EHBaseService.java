package com.enhantec.common.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.common.model.EHBaseModel;

import java.io.Serializable;
import java.util.List;

public interface EHBaseService<T extends EHBaseModel> extends IService<T> {

    T saveOrUpdateTr(T model)  throws IllegalAccessException;


    default T getById(Serializable id) {
        return  ((EHBaseMapper<T>) this.getBaseMapper()).selectByIdTr(id);
    }

    default List<T> list() {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectListTr(Wrappers.emptyWrapper());
    }


    /**
     * Currently we only implement this updateById method support optimisticLock throw Exception.
     * @param entity
     * @return
     */
    @Override
    default boolean updateById(T entity) {
        boolean isUpdateSuccess = SqlHelper.retBool(this.getBaseMapper().updateById(entity));
        if(!isUpdateSuccess) throw new EHApplicationException("c-sys-dataChangedByOtherUser");

        return true;
    }
}
