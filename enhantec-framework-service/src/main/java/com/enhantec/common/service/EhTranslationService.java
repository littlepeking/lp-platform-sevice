package com.enhantec.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.common.model.EhTranslation;

import java.io.Serializable;

/**
* @author johnw
* @description 针对表【eh_translation】的数据库操作Service
* @createDate 2022-09-22 16:07:04
*/
public interface EhTranslationService extends IService<EhTranslation> {

   EhTranslation find(String tableName, String columnName, String languageCode, String transId, boolean allowUseFallbackLang);

   EhTranslation findDefault(String tableName, String columnName, String transId);

   void remove(String tableName, String columnName, Serializable transId);
}
