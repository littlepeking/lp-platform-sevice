package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName test_receipt
 */
@TableName(value ="test_receipt")
@Data
public class TestReceipt implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private String receiptKey;

    /**
     * 
     */
    private String whseId;

    /**
     * 
     */
    private BigDecimal quantity;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}