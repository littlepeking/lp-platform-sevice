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

package com.enhantec.wms.ui.inbound.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHWMSModel;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.framework.config.annotations.FieldNameConversion;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @TableName test_receipt
 */
@TableName(value ="RECEIPT")
@FieldNameConversion(EHFieldNameConversionType.NONE)
@Data
public class ReceiptModel extends EHWMSModel implements Serializable {


    @TableField(value = "whseId")
    private String whseId;

    @TableField(value = "storerKey")
    private String storerKey;

    @TableField(value = "receiptKey")
    private String receiptKey;

    @TableField(value = "poKey")
    private String poKey;

    @TableField(value = "receiptDate")
    private LocalDateTime receiptDate;

    @TableField(value = "externReceiptKey")
    private String externReceiptKey;

    @TableField(value = "externalReceiptKey2")
    private String externalReceiptKey2;

    @TableField(value = "status")
    private String status;

    @TableField(value = "susr1")
    private String susr1;

    @TableField(value = "susr2")
    private String susr2;

    @TableField(value = "susr3")
    private String susr3;

    @TableField(value = "susr4")
    private String susr4;

    @TableField(value = "susr5")
    private String susr5;

    @TableField(value = "type")
    private String type;

    @TableField(value = "notes")
    private String notes;

    @TableField(value = "closedDate")
    private LocalDateTime closedDate;

    @TableField(value = "isConfirmed")
    private String isConfirmed;

    @TableField(value = "isConfirmedUser")
    private Boolean isConfirmedUser;

    @TableField(value = "isConfirmedUser2")
    private Boolean isConfirmedUser2;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

