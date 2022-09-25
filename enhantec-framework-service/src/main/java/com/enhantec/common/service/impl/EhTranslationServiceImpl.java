package com.enhantec.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.mapper.EhTranslationMapper;
import com.enhantec.common.model.EhTranslation;
import com.enhantec.common.service.EhTranslationService;
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
    implements EhTranslationService{

    final private LocaleResolver localResolver;

    public EhTranslation find(String tableName, String columnName, String languageCode, String transId, boolean allowUseFallbackLang){
      EhTranslation translation = baseMapper.findByTranslationKeys( tableName, columnName, languageCode, transId);

      if(translation==null && allowUseFallbackLang){
          //try fall back to default language
          String defLanguageCode = ((AcceptHeaderLocaleResolver)localResolver).getDefaultLocale().toString().replace('_','-');
          translation = baseMapper.findByTranslationKeys( tableName, columnName, defLanguageCode, transId);
      }

      return translation;

    }

    public void remove(String tableName, String columnName, Serializable transId){
        baseMapper.deleteByMap(Map.of(
                "table_name", tableName,
                "column_name", columnName,
                "trans_id", transId
        ));

    }

}




