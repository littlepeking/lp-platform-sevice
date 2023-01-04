/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.wms.inbound.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.framework.common.model.EHBaseModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    /**
     *
     * @TableName test_receipt
     */
    @TableName(value ="TEST_RECEIPT")
    @Data
    public class TestReceipt extends EHBaseModel implements Serializable {

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

        /**
         *
         */
        private String city;

        /**
         *
         */
        private LocalDateTime createTime;

        /**
         *
         */
        private Integer enabled;

        @TableField(exist = false)
        private static final long serialVersionUID = 1L;
    }

