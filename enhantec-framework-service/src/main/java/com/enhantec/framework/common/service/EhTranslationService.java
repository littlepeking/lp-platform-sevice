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



package com.enhantec.framework.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.framework.common.model.EhTranslation;

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
