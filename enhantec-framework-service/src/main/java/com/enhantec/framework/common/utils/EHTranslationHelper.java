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



package com.enhantec.framework.common.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHBaseModel;
import com.enhantec.framework.common.model.EhTranslation;
import com.enhantec.framework.common.service.EhTranslationService;
import com.enhantec.framework.config.TransFieldConfig;
import com.enhantec.framework.config.annotations.TransField;
import com.enhantec.framework.config.mybatisplus.MybatisPlusConfig;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EHTranslationHelper {

    @SneakyThrows
    public static <T extends EHBaseModel> void saveTranslation(T model) {

//        IFieldNameConverter fieldNameConverter = getFieldNameConverterByClass(model.getClass());

        for (Field field : model.getClass().getDeclaredFields()) {

            Class type = field.getType();

            TableField fieldAnnotation = field.getAnnotation(TableField.class);

            String columnNameInAnnotation = fieldAnnotation != null ? fieldAnnotation.value() : null;

            String columnName = StringUtils.isNotEmpty(columnNameInAnnotation) ? columnNameInAnnotation :
                   MybatisPlusConfig.getDefaultFieldNameConverter().convertFieldName2ColumnName(field.getName());


//            String columnName = fieldNameConverter.convertFieldName2ColumnName(field.getName());

            if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                String languageCode = EHContextHelper.getLanguageCode();

                String translateId = model.getId();

                TableName tableNameAnnotation = model.getClass().getAnnotation(TableName.class);

                String tableName = StringUtils.upperCase(tableNameAnnotation.value());

                field.setAccessible(true);
                Object text = field.get(model) !=null ? field.get(model) : "";

                if (translateId != null) {
                    EhTranslation translation;

                    translation = translationService.find(tableName, columnName, languageCode, translateId, false);
                    if (translation != null) {
                        translation.setTransText(text.toString());
                    } else {
                        translation = EhTranslation.builder().
                                tableName(tableName).
                                columnName(columnName).
                                transId(translateId).
                                languageCode(languageCode)
                                .transText(StringUtils.defaultString(text.toString(), ""))
                                .build();
                    }
                    translationService.saveOrUpdate(translation);
                }

                //Set field to default language translation in base table after get translation value from entity.
                EhTranslation defTranslation = translationService.findDefault(tableName, columnName, translateId);
                if (defTranslation != null) {
                    field.set(model, defTranslation.getTransText());
                }



            }

        }
    }

//    private static <T extends EHBaseModel> IFieldNameConverter getFieldNameConverterByClass(Class<T> clazz) {
//        IFieldNameConverter fieldNameConverter;
//        if (clazz.isAnnotationPresent(FieldNameConversion.class)) {
//           if(EHFieldNameConversionType.CAMELCASE2UNDERSCORE == clazz.getAnnotation(FieldNameConversion.class).value()){
//               fieldNameConverter = new CamelCase2UnderScoreConverter();
//           }else{
//               //case: EHFieldNameConversionType.NONE == model.getClass().getAnnotation(FieldNameConversion.class).value()
//                fieldNameConverter = new NoConverter();
//            }
//        }else{
//            fieldNameConverter = MybatisPlusConfig.getDefaultFieldNameConverter();
//        }
//        return fieldNameConverter;
//    }

    public static <T extends EHBaseModel> void saveTranslation(Collection<T> models) {
        models.forEach(model-> saveTranslation(model));
    }

    public static <T extends EHBaseModel> void deleteTranslationById(Serializable id, Class<T> clazz) {

        for (Field field : clazz.getDeclaredFields()) {
            Class type = field.getType();
            String columnName = field.getName();
            if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);

                String tableName = tableNameAnnotation.value();

                translationService.remove(tableName,columnName, id);


            }

        }
    }

    public static  <E extends Serializable, T extends EHBaseModel> void deleteTranslationByIds(Collection<E> ids, Class<T> clazz) {
        if(ids!=null || ids.size()>0){
            ids.forEach(id-> deleteTranslationById(id, clazz));
        }
    }



    public static <T extends EHBaseModel> void deleteTranslation(T model) {

        for (Field field : model.getClass().getDeclaredFields()) {
            Class type = field.getType();
            String columnName = field.getName();
            if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                String transId = model.getId();

                TableName tableNameAnnotation = model.getClass().getAnnotation(TableName.class);

                String tableName = tableNameAnnotation.value();

                translationService.remove(tableName,columnName, transId);

            }

        }
    }


    public static <T extends EHBaseModel> void deleteTranslation(Collection<T> models) {
        models.forEach(model-> deleteTranslation(model));
    }

    @SneakyThrows
    public static <T extends EHBaseModel> T translate(T model) {

        if(model!=null) {

            //  IFieldNameConverter fieldNameConverter = getFieldNameConverterByClass(model.getClass());

            for (Field field : model.getClass().getDeclaredFields()) {

                Class type = field.getType();

                TableField fieldAnnotation = field.getAnnotation(TableField.class);

                String columnNameInAnnotation = fieldAnnotation != null ? fieldAnnotation.value() : null;

                String columnName = StringUtils.isNotEmpty(columnNameInAnnotation) ? columnNameInAnnotation :
                        MybatisPlusConfig.getDefaultFieldNameConverter().convertFieldName2ColumnName(field.getName());


            //  String columnName =fieldNameConverter.convertFieldName2ColumnName(field.getName());
                if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                    EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                    String languageCode = EHContextHelper.getLanguageCode();

                    String translateId = model.getId();

                    TableName tableNameAnnotation = model.getClass().getAnnotation(TableName.class);

                    String tableName = tableNameAnnotation.value();

                    EhTranslation translation = translationService.find(tableName, columnName, languageCode, translateId, true);

                    if (translation != null) {

                        field.setAccessible(true);
                        field.set(model, translation.getTransText());

                    }

                }

            }
        }

        return model;

    }

    public static <T extends EHBaseModel> List<Map<String, Object>> translate(List<Map<String, Object>> dataList, List<TransFieldConfig> transFieldConfigList) {

        if(transFieldConfigList!=null && transFieldConfigList.size()>0) {

            EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);
            String languageCode = EHContextHelper.getLanguageCode();

            dataList.forEach(dataMap -> {
                transFieldConfigList.forEach(transFieldConfig -> {
                    String translateId = dataMap.get(transFieldConfig.getTransIdFieldName()) == null ? null : dataMap.get(transFieldConfig.getTransIdFieldName()).toString();
                    if (translateId != null) {
                        EhTranslation translation = translationService.find(transFieldConfig.getTransTableName(),
                                transFieldConfig.getTransColumnName(), languageCode, translateId, true);
                        if (translation != null) {
                            dataMap.put(transFieldConfig.getTransTextFieldName(), translation.getTransText());
                        }
                    }
                });
            });
        }

        return dataList;
    }

    public static <T extends EHBaseModel> Collection<T> translate(Collection<T> models) {

        models.forEach(model->translate(model));

        return models;
    }
}
