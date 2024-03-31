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
import com.enhantec.framework.common.exception.EHApplicationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EHCommonModel extends EHBaseModel{

    @TableId
    String id;

    @TableField(value = "ADD_DATE", fill = FieldFill.INSERT)
    LocalDateTime addDate;

    @TableField(value = "ADD_WHO", fill = FieldFill.INSERT)
    String addWho;

    @TableField(value = "EDIT_DATE", fill = FieldFill.INSERT_UPDATE)
    LocalDateTime editDate;

    @TableField(value = "EDIT_WHO", fill = FieldFill.INSERT_UPDATE)
    String editWho;

    @Override
    public String getId(){
       return id;
    }
    @Override
    public void setId(String id){
        this.id =id;
    }

    @Override
    public void setAddWho(String addWho){
        this.addWho = addWho;
    }
    @Override
    public String getAddWho(){
       return addWho;
    }
    @Override
    public void setAddDate(LocalDateTime addDate){
        this.addDate = addDate;
    }
    @Override
    public LocalDateTime getAddDate(){
        return addDate;
    }
    @Override
    public void setEditWho(String editWho){
        this.editWho = editWho;
    }
    @Override
    public String getEditWho(){
        return editWho;
    }
    @Override
    public void setEditDate(LocalDateTime editDate){
        this.editDate = editDate;
    }
    @Override
    public LocalDateTime getEditDate(){
       return editDate;
    }


}
