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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHBaseModel;
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
public class ReceiptModel extends EHBaseModel implements Serializable {

//    @TableId
    private String serialKey;

    public String getId(){
        return serialKey;
    }

    public void setId(String serialKey){
        this.serialKey = serialKey;
    }


    private String whseId;
    private String storerKey;
    private String receiptKey;
    private String poKey;
    private LocalDateTime receiptDate;
    private String externReceiptKey;
    private String externReceiptKey2;
    private String packKey;
    private String uom;
    private String status;
    private String susr1;
    private String susr2;
    private String susr3;
    private String susr4;
    private String susr5;
    private String type;
    private String notes;
    private LocalDateTime closeDate;
    private String isConfirmed;
    private Boolean isConfirmedUser1;
    private Boolean isConfirmedUser2;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

