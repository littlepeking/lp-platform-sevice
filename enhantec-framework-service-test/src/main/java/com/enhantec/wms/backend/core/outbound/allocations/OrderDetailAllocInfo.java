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

package com.enhantec.wms.backend.core.outbound.allocations;

import lombok.Data;

import java.util.Map;
import java.math.BigDecimal;

@Data
public class OrderDetailAllocInfo {

    private String storerKey;

    private String orderKey;

    private String orderLineNumber;

    private String orderType;

    private String sku;

    private String idRequired;

    private String allocationStrategyKey;
    //存储当前分配使用的分配策略明细记录
    private Map<String,String> currentAllocStrategyDetail;

    //是否整容器分配(TODO 存至上面的分配策略的明细记录中)
    //private boolean isFullLpnAlloc;

    private BigDecimal qtyToBeAllocate;

    private AllocationResult result = new AllocationResult();

    private CurrentAllocLotInfo currentAllocLotInfo = new CurrentAllocLotInfo();

    public enum AllocStatus {
        fullyAllocated,
        partialAllocated,
        notAllocated
    }

    //used by hard allocation
    @Data
    public class CurrentAllocLotInfo {

        private String lot;

        private BigDecimal unHoldQtyAvail;

    }

    //分配结果
    @Data
    public class AllocationResult {

        private AllocStatus allocStatus = AllocStatus.notAllocated;

        private String messageInfo = "";

    }
}