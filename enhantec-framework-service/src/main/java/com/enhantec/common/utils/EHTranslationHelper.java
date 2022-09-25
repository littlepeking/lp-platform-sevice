package com.enhantec.common.utils;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHBaseModel;
import com.enhantec.common.model.EhTranslation;
import com.enhantec.common.service.EhTranslationService;
import com.enhantec.config.TransFieldConfig;
import com.enhantec.config.annotations.TransField;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class EHTranslationHelper {

    @SneakyThrows
    public static <T extends EHBaseModel> void saveTranslation(T model) {

        for (Field field : model.getClass().getDeclaredFields()) {
            Class type = field.getType();
            String columnName = field.getName();
            if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                String languageCode = EHContextHelper.getLanguageCode();

                String translateId = model.getId();

                TableName tableNameAnnotation = model.getClass().getAnnotation(TableName.class);

                String tableName = tableNameAnnotation.value();

                field.setAccessible(true);
                Object text = field.get(model);

                if (text != null) {
                    EhTranslation translation = translationService.find(tableName, columnName, languageCode, translateId,false);
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

            }

        }
    }

    public static <T extends EHBaseModel> void saveTranslation(List<T> models) {
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


    public static <T extends EHBaseModel> void deleteTranslation(List<T> models) {
        models.forEach(model-> deleteTranslation(model));
    }

    @SneakyThrows
    public static <T extends EHBaseModel> T translate(T model) {


        for (Field field : model.getClass().getDeclaredFields()) {
            Class type = field.getType();
            String columnName = field.getName();
            if (type == String.class && field.isAnnotationPresent(TransField.class)) {

                EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);

                String languageCode = EHContextHelper.getLanguageCode();

                String translateId = model.getId();

                TableName tableNameAnnotation = model.getClass().getAnnotation(TableName.class);

                String tableName = tableNameAnnotation.value();

                EhTranslation translation = translationService.find(tableName, columnName, languageCode, translateId,true);

                if (translation != null) {

                    field.setAccessible(true);
                    field.set(model, translation.getTransText());

                }

            }

        }

        return model;

    }

    public static <T extends EHBaseModel> List<Map<String, Object>> translate(List<Map<String, Object>> dataList, List<TransFieldConfig> transFieldConfigList) {

        EhTranslationService translationService = EHContextHelper.getBean(EhTranslationService.class);
        String languageCode = EHContextHelper.getLanguageCode();

        dataList.forEach(dataMap->{
           transFieldConfigList.forEach(transFieldConfig -> {
               String translateId = dataMap.get(transFieldConfig.getTransIdFieldName()) == null ? null : dataMap.get(transFieldConfig.getTransIdFieldName()).toString();
               if(translateId != null){
                   EhTranslation translation = translationService.find(transFieldConfig.getTransTableName(),
                           transFieldConfig.getTransColumnName(), languageCode, translateId,true);
                   if(translation!=null) {
                       dataMap.put(transFieldConfig.getTransTextFieldName(), translation.getTransText());
                   }
               }
           });
        });

        return dataList;
    }

    public static <T extends EHBaseModel> List<T> translate(List<T> models) {

        models.forEach(model->translate(model));

        return models;
    }
}
