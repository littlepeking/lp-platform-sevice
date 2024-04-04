/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/



package com.enhantec.framework.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.framework.common.model.EHBaseModel;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.common.utils.EHTranslationHelper;
import com.enhantec.framework.config.TransFieldConfig;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import lombok.val;
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

            ///add translation
            EHTranslationHelper.saveTranslation(entity);
            ///

            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap();
            param.put("et", entity);
            sqlSession.update(this.getSqlStatement(SqlMethod.UPDATE_BY_ID), param);
        });
    }

    @Transactional(
            rollbackFor = {Exception.class}
    )
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        String sqlStatement = this.getSqlStatement(SqlMethod.UPDATE_BY_ID);
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {

            ///add translation
            EHTranslationHelper.saveTranslation(entity);

            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap();
            param.put("et", entity);
            sqlSession.update(sqlStatement, param);
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

    public Page<Map<String,Object>> queryPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        return getBaseMapper().queryPageData(page, qw);

    }

    public Page<Map<String,Object>> queryPageData(PageParams pageParams){

        return this.queryPageData(pageParams,null);

    }

    public Page<Map<String,Object>> queryPageData(PageParams pageParams, EHFieldNameConversionType fieldNameConversionType){

        Page pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams,fieldNameConversionType);

        return getBaseMapper().queryPageData(pageInfo, queryWrapper);
    }




}
