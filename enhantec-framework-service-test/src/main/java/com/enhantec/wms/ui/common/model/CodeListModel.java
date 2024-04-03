package com.enhantec.wms.ui.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.enhantec.framework.common.model.EHWMSModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@TableName(value ="CODELIST")
@SuperBuilder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeListModel extends EHWMSModel implements Serializable {
    /**
     * 
     */
    @TableField(value = "LISTNAME")
    private String listName;
    /**
     * 
     */
    @TableField(value = "WHSEID")
    private String whseId;

    /**
     * 
     */
    @TableField(value = "DESCRIPTION")
    private String description;

    /**
     * 
     */
    @TableField(value = "SOURCEVERSION")
    private Integer sourceVersion;

    /**
     * 
     */
    @TableField(value = "EDITABLE")
    private String editable;

    /**
     * 
     */
    @TableField(value = "SUBDETAIL")
    private String subDetail;

    /**
     * 
     */
    @TableField(value = "PARENTLIST")
    private String parentList;

    /**
     * 
     */
    @TableField(value = "PARENTCODE")
    private String parentCode;

    /**
     * 
     */
    @TableField(value = "ENTERPRISECODE")
    private String enterpriseCode;

    /**
     * 
     */
    @TableField(value = "USEDEXTERNALLYINDICATOR")
    private String usedExternallyIndicator;

    /**
     * 
     */
    @TableField(value = "LANGUAGE")
    private String language;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}