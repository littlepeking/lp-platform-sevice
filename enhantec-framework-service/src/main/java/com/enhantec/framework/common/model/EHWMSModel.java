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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EHWMSModel extends EHBaseModel{

    @TableId("serialKey")
    String serialKey;

    public String getId(){
        return serialKey;
    }

    public void setId(String serialKey){
        this.serialKey = serialKey;
    }

    @TableField(value = "addDate", fill = FieldFill.INSERT)
    LocalDateTime addDate;

    @TableField(value = "addWho",fill = FieldFill.INSERT)
    String addWho;

    @TableField(value = "editDate",fill = FieldFill.INSERT_UPDATE)
    LocalDateTime editDate;

    @TableField(value = "editWho",fill = FieldFill.INSERT_UPDATE)
    String editWho;

}