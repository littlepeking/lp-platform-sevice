/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.common.service.EHBaseService;
import com.enhantec.common.utils.EHTranslationHelper;
import com.enhantec.config.TransFieldConfig;
import org.apache.ibatis.binding.MapperMethod;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EHBaseServiceImpl<M extends EHBaseMapper<T>, T extends EHBaseModel> extends ServiceImpl<M, T> implements EHBaseService<T> {

    //To resolve original saveOrUpdate method return partial entity with updated columns only.
    @Transactional(rollbackFor = Exception.class)
    public T saveOrUpdateRetE(T model) {

        //No need to save transaction here as super.saveOrUpdate will call save or updateById method and both 2 methods already implemented translation.
        //EHTranslationHelper.saveTranslation(model);

        super.saveOrUpdate(model);

        return baseMapper.selectByIdTr(model.getId());

    }

    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(T model) {
        throw new RuntimeException("Please use saveOrUpdateRetE instead of saveOrUpdate.");
    }

    //////

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        String sqlStatement = this.getSqlStatement(SqlMethod.INSERT_ONE);

        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            sqlSession.insert(sqlStatement, entity);
            ///add translation
            EHTranslationHelper.saveTranslation(entity);
            ///
        });
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize) {

        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!", new Object[0]);
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!", new Object[0]);
        return SqlHelper.saveOrUpdateBatch(this.entityClass, this.mapperClass, this.log, entityList, batchSize, (sqlSession, entity) -> {
            Object idVal = tableInfo.getPropertyValue(entity, keyProperty);
            return StringUtils.checkValNull(idVal) || CollectionUtils.isEmpty(sqlSession.selectList(this.getSqlStatement(SqlMethod.SELECT_BY_ID), entity));
        }, (sqlSession, entity) -> {
            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap();
            param.put("et", entity);
            sqlSession.update(this.getSqlStatement(SqlMethod.UPDATE_BY_ID), param);
            ///add translation
            EHTranslationHelper.saveTranslation(entity);
            ///
        });
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        String sqlStatement = this.getSqlStatement(SqlMethod.UPDATE_BY_ID);
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap();
            param.put("et", entity);
            sqlSession.update(sqlStatement, param);
            ///add translation
            EHTranslationHelper.saveTranslation(entity);
            ///
        });
    }

    public T getOne(Wrapper<T> queryWrapper, boolean throwEx) {
        return throwEx ? this.baseMapper.selectOneTr(queryWrapper) : SqlHelper.getObject(this.log, this.baseMapper.selectListTr(queryWrapper));
    }

    public Map<String, Object> getMap(Wrapper<T> queryWrapper, List<TransFieldConfig> translationConfigList) {
        return SqlHelper.getObject(this.log, this.baseMapper.selectMapsTr(queryWrapper, translationConfigList));
    }
    /**
     * Please use method getMap(Wrapper<T> queryWrapper, List<TransFieldConfig> translationConfigList) to enable translation
     * @param queryWrapper
     * @return
     */
    @Deprecated
    public Map<String, Object> getMap(Wrapper<T> queryWrapper) {
        return SqlHelper.getObject(this.log, this.baseMapper.selectMaps(queryWrapper));
    }
    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean removeById(Serializable id) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
        return tableInfo.isWithLogicDelete() && tableInfo.isWithUpdateFill() ? this.removeById(id, true) : SqlHelper.retBool(this.getBaseMapper().deleteByIdTr(id));
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean removeByIds(Collection<?> list) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        } else {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(this.getEntityClass());
            return tableInfo.isWithLogicDelete() && tableInfo.isWithUpdateFill() ? this.removeBatchByIds(list, true) : SqlHelper.retBool(this.getBaseMapper().deleteBatchIdsTr(list));
        }
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean removeById(Serializable id, boolean useFill) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
        if (useFill && tableInfo.isWithLogicDelete() && !this.entityClass.isAssignableFrom(id.getClass())) {
            T instance = tableInfo.newInstance();
            tableInfo.setPropertyValue(instance, tableInfo.getKeyProperty(), new Object[]{id});
            return this.removeById(instance);
        } else {
            return SqlHelper.retBool(this.getBaseMapper().deleteByIdTr(id));
        }
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean removeBatchByIds(Collection<?> list, int batchSize, boolean useFill) {
        ///add translation
        try {
            //test if list is entity list
            EHTranslationHelper.deleteTranslation((Collection<T>)list);
        }catch(Exception e){
            Collection<String> ids;
            try {
                //test if list is String list
               ids = (Collection<String>)list;
               EHTranslationHelper.deleteTranslationByIds(ids, this.entityClass);
            }catch(Exception e2){
                throw new RuntimeException("list must be either entity list or id list");
            }

        };
      ////////////////

        String sqlStatement = this.getSqlStatement(SqlMethod.DELETE_BY_ID);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
        return this.executeBatch(list, batchSize, (sqlSession, e) -> {
            if (useFill && tableInfo.isWithLogicDelete()) {
                if (this.entityClass.isAssignableFrom(e.getClass())) {
                    sqlSession.update(sqlStatement, e);
                } else {
                    T instance = tableInfo.newInstance();
                    tableInfo.setPropertyValue(instance, tableInfo.getKeyProperty(), new Object[]{e});
                    sqlSession.update(sqlStatement, instance);
                }
            } else {
                sqlSession.update(sqlStatement, e);
            }

        });
    }

}
