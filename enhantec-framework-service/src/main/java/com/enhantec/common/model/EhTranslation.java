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