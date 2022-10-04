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



package com.enhantec.common.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.common.utils.EHTranslationHelper;
import com.enhantec.config.TransFieldConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface EHBaseMapper<T extends EHBaseModel> extends BaseMapper<T> {

    /**
     * Use insertTr to replace this method to handle translation logic.
     */
    @Deprecated
    int insert(T entity);
    /**
     * Use selectListTr to replace this method to handle translation logic.
     */
    @Deprecated
    List<T> selectList(@Param("ew") Wrapper<T> queryWrapper);


    /**
     * Use deleteByIdTr to replace this method to handle translation logic.
     */
    @Deprecated
    int deleteById(Serializable id);

    /**
     * Use deleteByIdTr to replace this method to handle translation logic.
     */
    @Deprecated
    int deleteById(T entity);
    /**
     *  Use deleteByMapTr to replace this method to handle translation logic.
     */
    @Deprecated
    int deleteByMap(@Param("cm") Map<String, Object> columnMap);
    /**
     *  Use deleteTr to replace this method to handle translation logic.
     */
    @Deprecated
    int delete(@Param("ew") Wrapper<T> queryWrapper);
    /**
     *  Use deleteBatchIdsTr to replace this method to handle translation logic.
     */
    @Deprecated
    int deleteBatchIds(@Param("coll") Collection<?> idList);
    /**
     *  Use updateByIdTr to replace this method to handle translation logic.
     */
    @Deprecated
    int updateById(@Param("et") T entity);
    /**
     *  Use updateTr to replace this method to handle translation logic.
     */
    @Deprecated
    int update(@Param("et") T entity, @Param("ew") Wrapper<T> updateWrapper);
    /**
     *  Use selectByIdTr to replace this method to handle translation logic.
     */
    @Deprecated
    T selectById(Serializable id);
    /**
     *  Use selectBatchIdsTr to replace this method to handle translation logic.
     */
    @Deprecated
    List<T> selectBatchIds(@Param("coll") Collection<? extends Serializable> idList);
    /**
     *  Use selectByMapTr to replace this method to handle translation logic.
     */
    @Deprecated
    List<T> selectByMap(@Param("cm") Map<String, Object> columnMap);

    /**
     *  Use selectOneTr to replace this method to handle translation logic.
     */
    @Deprecated
    default T selectOne(@Param("ew") Wrapper<T> queryWrapper) {
        List<T> ts = this.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(ts)) {
            if (ts.size() != 1) {
                throw ExceptionUtils.mpe("One record is expected, but the query result is multiple records", new Object[0]);
            } else {
                return ts.get(0);
            }
        } else {
            return null;
        }
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default boolean exists(Wrapper<T> queryWrapper) {
        Long count = this.selectCount(queryWrapper);
        return null != count && count > 0L;
    }

    /**
     *  Use selectMapsTr to replace this method to handle translation logic.
     */
    @Deprecated
    List<Map<String, Object>> selectMaps(@Param("ew") Wrapper<T> queryWrapper);

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper and return object is NOT a translation column.
     * @param queryWrapper
     * @return
     */
    List<Object> selectObjs(@Param("ew") Wrapper<T> queryWrapper);

    /**
     * Use selectPageTr to replace this method to handle translation logic.
     *
     */
    @Deprecated
    <P extends IPage<T>> P selectPage(P page, @Param("ew") Wrapper<T> queryWrapper);
    /**
     * Use selectMapsPageTr to replace this method to handle translation logic.
     *
     */
    @Deprecated
    <P extends IPage<Map<String, Object>>> P selectMapsPage(P page, @Param("ew") Wrapper<T> queryWrapper);

    @Transactional(rollbackFor = Exception.class)
    default int insertTr(T entity){

        int result = insert(entity);

        EHTranslationHelper.saveTranslation(entity);

        return result;

    }
    default int deleteByIdTr(Serializable id){

        Class<T> tClass = (Class<T>) ReflectionKit.getSuperClassGenericType(this.getClass(),EHBaseMapper.class,0);

        EHTranslationHelper.deleteTranslationById(id.toString(), tClass);

        return deleteById(id);
    }

    default int deleteByIdTr(T entity){

        EHTranslationHelper.deleteTranslation(entity);

        return deleteById(entity);

    }
    /**
     * This Method can be only used in cases when translation column NOT in columnMap.
     * @param columnMap
     * @return
     */
    default int deleteByMapTr(@Param("cm") Map<String, Object> columnMap){

        List<T> rows2Delete = selectByMap(columnMap);

        EHTranslationHelper.deleteTranslation(rows2Delete);

        return deleteByMap(columnMap);
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default int deleteTr(@Param("ew") Wrapper<T> queryWrapper){

        List<T> rows2Delete = selectList(queryWrapper);

        EHTranslationHelper.deleteTranslation(rows2Delete);

        return delete(queryWrapper);

    }

    default int deleteBatchIdsTr(@Param("coll") Collection<?> idList){

        List<T> rows2Delete = selectBatchIds((Collection<? extends Serializable>)idList);
        EHTranslationHelper.deleteTranslation(rows2Delete);
        return deleteBatchIds(idList);
    }

    default int updateByIdTr(@Param("et") T entity){

        EHTranslationHelper.saveTranslation(entity);
        return updateById(entity);

    }
    /**
     * This Method can be only used in cases when translation column NOT in updateWrapper.
     * @param updateWrapper
     * @return
     */
    default int updateTr(@Param("et") T entity, @Param("ew") Wrapper<T> updateWrapper) {

       int res = update(entity, updateWrapper);

        if(entity!=null) {
            EHTranslationHelper.saveTranslation(entity);
            ////Add translation and update entity translation fields to default language.
            updateById(entity);
            ////
        }
        if(updateWrapper!=null){
            List<T> rowsUpdated = selectList(updateWrapper);
            if(rowsUpdated.size()>0) {
                ////Add translation and update entity translation fields to default language.
                EHTranslationHelper.saveTranslation(rowsUpdated);
                ////
                rowsUpdated.forEach(row-> updateById(row));
            }
        }

        return res;

    }

    default T selectByIdTr(Serializable id){
       T entity = selectById(id);
       return EHTranslationHelper.translate(entity);
    }

    default List<T> selectBatchIdsTr(@Param("coll") Collection<? extends Serializable> idList){
        List<T> entities = selectBatchIds(idList);
        EHTranslationHelper.translate(entities);
        return entities;
    }

    /**
     * This Method can be only used in cases when translation column NOT in columnMap.
     * @param columnMap
     * @return
     */
    default List<T> selectByMapTr(@Param("cm") Map<String, Object> columnMap){
        List<T> entities = selectByMap(columnMap);
        EHTranslationHelper.translate(entities);
        return entities;
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default T selectOneTr(@Param("ew") Wrapper<T> queryWrapper) {
        T entity = selectOne(queryWrapper);
        return EHTranslationHelper.translate(entity);
    }
    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default List<T> selectListTr(@Param("ew") Wrapper<T> queryWrapper){
        List<T> entities = selectList(queryWrapper);
        EHTranslationHelper.translate(entities);
        return entities;
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default List<Map<String, Object>> selectMapsTr(@Param("ew") Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList){
       var list = selectMaps(queryWrapper);
       EHTranslationHelper.translate(list, translationConfigList);
       return list;
    }
//
//    /**
//     * 只查询一列的值。如果有多列返回，只返回第一个列
//     * @param queryWrapper
//     * @return
//     */
//    @Deprecated
//    default List<Object> selectObjsTr(@Param("ew") Wrapper<T> queryWrapper){
//        throw new RuntimeException("Not support selectObjs for translation, please translate manually.");
//    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
    default <P extends IPage<T>> P selectPageTr(P page, @Param("ew") Wrapper<T> queryWrapper){
        P pageData = selectPage(page,queryWrapper);

        if(pageData.getRecords().size()>0){
            EHTranslationHelper.translate(pageData.getRecords());
        }

        return pageData;
    }

    /**
     * This Method can be only used in cases when translation column NOT in queryWrapper.
     * @param queryWrapper
     * @return
     */
   default <P extends IPage<Map<String, Object>>> P selectMapsPageTr(P page, @Param("ew") Wrapper<T> queryWrapper,List<TransFieldConfig> translationConfigList){
       var list = selectMapsPage(page,queryWrapper);
       EHTranslationHelper.translate(list.getRecords(), translationConfigList);
       return list;
   }

}
