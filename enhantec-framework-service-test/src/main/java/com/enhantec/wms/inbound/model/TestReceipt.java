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

