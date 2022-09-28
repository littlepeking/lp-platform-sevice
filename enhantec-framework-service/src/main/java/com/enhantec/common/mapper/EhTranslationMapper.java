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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.common.model.EhTranslation;
import org.springframework.stereotype.Repository;

/**
* @author johnw
* @description 针对表【eh_translation】的数据库操作Mapper
* @createDate 2022-09-22 16:07:04
* @Entity com.enhantec.common.model.EhTranslation
*/
@Repository
public interface EhTranslationMapper extends BaseMapper<EhTranslation> {

    EhTranslation findByTranslationKeys(String tableName, String columnName, String languageCode, String transId);

}




