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



package com.enhantec.framework.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 *
 * @TableName EH_TRANSLATION
 */

@Data
@SuperBuilder
@TableName(value ="EH_TRANSLATION")
@NoArgsConstructor
@AllArgsConstructor
public class EhTranslation extends EHCommonModel implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    @TableField("TABLE_NAME")
    private String tableName;

    @TableField("COLUMN_NAME")
    private String columnName;

    @TableField("TRANS_ID")
    private String transId;

    @TableField("LANGUAGE_CODE")
    private String languageCode;

    @TableField("TRANS_TEXT")
    private String transText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}