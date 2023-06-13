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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.framework.common.mapper.EhTranslationMapper;
import com.enhantec.framework.common.model.EhTranslation;
import com.enhantec.framework.common.service.EhTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.io.Serializable;
import java.util.Map;

/**
* @author johnw
* @description 针对表【eh_translation】的数据库操作Service实现
* @createDate 2022-09-22 16:07:04
*/
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EhTranslationServiceImpl extends ServiceImpl<EhTranslationMapper, EhTranslation>
    implements EhTranslationService {

    final private LocaleResolver localResolver;

    public EhTranslation find(String tableName, String columnName, String languageCode, String transId, boolean allowUseFallbackLang){
      EhTranslation translation = baseMapper.findByTranslationKeys( tableName, columnName, languageCode, transId);

      if(translation==null && allowUseFallbackLang){
          //try fall back to default language
        translation = findDefault( tableName, columnName, transId);
      }

      return translation;

    }
    public EhTranslation findDefault(String tableName, String columnName, String transId){

            String defLanguageCode = ((AcceptHeaderLocaleResolver)localResolver).getDefaultLocale().toString().replace('_','-');
            return baseMapper.findByTranslationKeys( tableName, columnName, defLanguageCode, transId);

    }

    public void remove(String tableName, String columnName, Serializable transId){
        baseMapper.deleteByMap(Map.of(
                "table_name", tableName,
                "column_name", columnName,
                "trans_id", transId
        ));

    }

}




