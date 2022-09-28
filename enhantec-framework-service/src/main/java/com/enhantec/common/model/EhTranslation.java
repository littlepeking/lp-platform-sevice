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



package com.enhantec.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName eh_translation
 */

@Data
@SuperBuilder
@TableName(value ="eh_translation")
@NoArgsConstructor
@AllArgsConstructor
public class EhTranslation extends EHBaseModel implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private String tableName;

    /**
     * 
     */
    private String columnName;

    /**
     * 
     */
    private String transId;

    /**
     * 
     */
    private String languageCode;

    /**
     * 
     */
    private String transText;

    /**
     * 
     */
    private String addWho;

    /**
     * 
     */
    private LocalDateTime addDate;

    /**
     * 
     */
    private String editWho;

    /**
     * 
     */
    private LocalDateTime editDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}