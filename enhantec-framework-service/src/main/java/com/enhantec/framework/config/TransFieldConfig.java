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



package com.enhantec.framework.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TransFieldConfig {

    private String transIdFieldName;//翻译所依据的 ID - TransId 在目标对象存储的Field
    private String transTextFieldName;//找到翻译值后写入目标对象的FieldName(待翻译的属性字段名)
    private String transTableName;//在翻译表中需要检索的表名
    private String transColumnName;//在翻译表中需要检索的列名

}
