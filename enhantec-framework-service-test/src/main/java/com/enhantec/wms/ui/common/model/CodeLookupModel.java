package com.enhantec.wms.ui.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.enhantec.framework.common.model.EHWMSModel;
import com.enhantec.framework.config.annotations.TransField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@TableName(value ="CODELKUP")
@SuperBuilder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeLookupModel extends EHWMSModel implements Serializable {

    @TableField(value = "LISTNAME")
    private String listName;

    /**
     * 
     */
    @TableField(value = "CODE")
    private String code;

    /**
     * 
     */
    @TableField(value = "WHSEID")
    private String whseId;

    @TransField
    @TableField(value = "DESCRIPTION")
    private String description;

    @TransField
    @TableField(value = "SHORT")
    private String shortValue;

    @TransField
    @TableField(value = "LONG_VALUE")
    private String longValue;

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
    @TableField(value = "ACTIVE")
    private String active;

    /**
     * 
     */
    @TableField(value = "SEQUENCE")
    private Integer sequence;

    /**
     * 
     */
    @TableField(value = "UDF1")
    private String udf1;

    /**
     * 
     */
    @TableField(value = "UDF2")
    private String udf2;

    /**
     * 
     */
    @TableField(value = "UDF3")
    private String udf3;

    /**
     * 
     */
    @TableField(value = "UDF4")
    private String udf4;

    /**
     * 
     */
    @TableField(value = "UDF5")
    private String udf5;

    /**
     *
     */
    @TableField(value = "UDF6")
    private String udf6;

    /**
     *
     */
    @TableField(value = "UDF7")
    private String udf7;

    /**
     *
     */
    @TableField(value = "UDF8")
    private String udf8;

    /**
     *
     */
    @TableField(value = "UDF9")
    private String udf9;

    /**
     *
     */
    @TableField(value = "UDF10")
    private String udf10;
    /**
     * 
     */
    @TableField(value = "NOTES")
    private String notes;

    /**
     * 
     */
    @TableField(value = "WOFLAG")
    private Integer woFlag;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR1")
    private String extUdfStr1;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR2")
    private String extUdfStr2;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR3")
    private String extUdfStr3;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR4")
    private String extUdfStr4;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR5")
    private String extUdfStr5;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR6")
    private String extUdfStr6;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR7")
    private String extUdfStr7;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR8")
    private String extUdfStr8;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR9")
    private String extUdfStr9;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR10")
    private String extUdfStr10;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR11")
    private String extUdfStr11;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR12")
    private String extUdfStr12;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR13")
    private String extUdfStr13;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR14")
    private String extUdfStr14;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_STR15")
    private String extUdfStr15;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_DATE1")
    private LocalDateTime extUdfDate1;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_DATE2")
    private LocalDateTime extUdfDate2;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_DATE3")
    private LocalDateTime extUdfDate3;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_DATE4")
    private LocalDateTime extUdfDate4;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_DATE5")
    private LocalDateTime extUdfDate5;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_FLOAT1")
    private Double extUdfFloat1;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_FLOAT2")
    private Double extUdfFloat2;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_FLOAT3")
    private Double extUdfFloat3;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_FLOAT4")
    private Double extUdfFloat4;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_FLOAT5")
    private Double extUdfFloat5;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP1")
    private String extUdfLkup1;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP2")
    private String extUdfLkup2;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP3")
    private String extUdfLkup3;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP4")
    private String extUdfLkup4;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP5")
    private String extUdfLkup5;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP6")
    private String extUdfLkup6;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP7")
    private String extUdfLkup7;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP8")
    private String extUdfLkup8;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP9")
    private String extUdfLkup9;

    /**
     * 
     */
    @TableField(value = "EXT_UDF_LKUP10")
    private String extUdfLkup10;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}