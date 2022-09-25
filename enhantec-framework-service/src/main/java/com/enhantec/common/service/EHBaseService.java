package com.enhantec.common.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.config.TransFieldConfig;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EHBaseService<T extends EHBaseModel> extends IService<T> {

    default boolean save(T entity) {
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).insertTr(entity));
    }


    default boolean removeById(Serializable id) {
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteByIdTr(id));
    }


    default boolean removeById(T entity) {
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteByIdTr(entity));
    }

    default boolean removeByMap(Map<String, Object> columnMap) {
        Assert.notEmpty(columnMap, "error: columnMap must not be empty", new Object[0]);
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteByMapTr(columnMap));
    }

    default boolean remove(Wrapper<T> queryWrapper) {
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteTr(queryWrapper));
    }

    default boolean removeByIds(Collection<?> list) {
        return CollectionUtils.isEmpty(list) ? false : SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteBatchIdsTr(list));
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean removeByIds(Collection<?> list, boolean useFill) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        } else {
            return useFill ? this.removeBatchByIds(list, true) : SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).deleteBatchIdsTr(list));
        }
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean removeBatchByIds(Collection<?> list) {
        return this.removeBatchByIds(list, 1000);
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean removeBatchByIds(Collection<?> list, boolean useFill) {
        return this.removeBatchByIds(list, 1000, useFill);
    }

    default boolean removeBatchByIds(Collection<?> list, int batchSize) {
        throw new UnsupportedOperationException("不支持的方法!");
    }

    default boolean removeBatchByIds(Collection<?> list, int batchSize, boolean useFill) {
        throw new UnsupportedOperationException("不支持的方法!");
    }

    /**
     * Currently we only implement this updateById method support optimisticLock throw Exception.
     * @param entity
     * @return
     */
    @Override
    default boolean updateById(T entity) {
        boolean isUpdateSuccess = SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).updateByIdTr(entity));
        if(!isUpdateSuccess) throw new EHApplicationException("c-sys-dataChangedByOtherUser");

        return true;
    }

    /**
     * This Method can be only used in cases when translation column NOT in updateWrapper.
     * @param updateWrapper
     * @return
     */
    default boolean update(Wrapper<T> updateWrapper) {
        return this.update(null, updateWrapper);
    }

    /**
     * This Method can be only used in cases when translation column NOT in updateWrapper.
     * @param updateWrapper
     * @return
     */
    default boolean update(T entity, Wrapper<T> updateWrapper) {
        return SqlHelper.retBool(((EHBaseMapper<T>) this.getBaseMapper()).updateTr(entity, updateWrapper));
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    default boolean updateBatchById(Collection<T> entityList) {
        return this.updateBatchById(entityList, 1000);
    }

    default T getById(Serializable id) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectByIdTr(id);
    }

    default List<T> listByIds(Collection<? extends Serializable> idList) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectBatchIdsTr(idList);
    }

    /**
     * This Method can be only used in cases when translation column NOT in columnMap.
     * @param columnMap
     * @return
     */
    default List<T> listByMap(Map<String, Object> columnMap) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectByMapTr(columnMap);
    }
    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default List<T> list(Wrapper<T> queryWrapper) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectListTr(queryWrapper);
    }

    default List<T> list() {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectListTr(Wrappers.emptyWrapper());
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default <E extends IPage<T>> E page(E page, Wrapper<T> queryWrapper) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectPageTr(page, queryWrapper);
    }

    default <E extends IPage<T>> E page(E page) {
        return this.page(page, Wrappers.emptyWrapper());
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default List<Map<String, Object>> listMaps(Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList) {
        return ((EHBaseMapper<T>) this.getBaseMapper()).selectMapsTr(queryWrapper,translationConfigList);
    }

    /**
     * Please use method listMaps(Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList) to enable translation
     * @param queryWrapper
     * @return
     */
    @Deprecated
    default List<Map<String, Object>> listMaps(Wrapper<T> queryWrapper) {
        return listMaps(queryWrapper,new ArrayList<>());
    }

    default List<Map<String, Object>> listMaps(List<TransFieldConfig> translationConfigList) {
        return this.listMaps(Wrappers.emptyWrapper(),translationConfigList);
    }

    /**
     * Please use method listMaps(List<TransFieldConfig> translationConfigList) to enable translation
     * @return
     */
    @Deprecated
    default List<Map<String, Object>> listMaps() {
        return this.listMaps(Wrappers.emptyWrapper());
    }


    default <E extends IPage<Map<String, Object>>> E pageMaps(E page, Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList) {
        return  ((EHBaseMapper<T>) this.getBaseMapper()).selectMapsPageTr(page, queryWrapper,translationConfigList);
    }
    /**
     * Please use method pageMaps(E page, Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList) to enable translation
     * @return
     */
    @Deprecated
    default <E extends IPage<Map<String, Object>>> E pageMaps(E page, Wrapper<T> queryWrapper) {
        return this.getBaseMapper().selectMapsPage(page, queryWrapper);
    }

    default <E extends IPage<Map<String, Object>>> E pageMaps(E page,List<TransFieldConfig> translationConfigList) {
        return this.pageMaps(page, Wrappers.emptyWrapper(), translationConfigList);
    }

    /**
     * Please use method pageMaps(E page,List<TransFieldConfig> translationConfigList) to enable translation
     * @return
     */
    @Deprecated
    default <E extends IPage<Map<String, Object>>> E pageMaps(E page) {
        return this.pageMaps(page, Wrappers.emptyWrapper());
    }

    T saveOrUpdateRetE(T model);

}
