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

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.outbound.AllocationStrategy;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.core.WMSServiceNames;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;


@Service
@AllArgsConstructor
public class AllocationService {

    private void allocateOrderLine(String orderKey, String orderLineNumber){

        AllocInfo allocInfo = buildOrderLineAllocInfo(orderKey,orderLineNumber);

        allocateOrderLine(allocInfo);

    }

    private AllocInfo buildOrderLineAllocInfo(String orderKey, String orderLineNumber){

        Map<String, Object> orderDetailInfo = Orders.findOrderDetail(orderKey, orderLineNumber);

        AllocInfo allocInfo = new AllocInfo();
        allocInfo.setOrderKey(orderKey);
        allocInfo.setOrderLineNumber(orderLineNumber);
        allocInfo.setOrderType(orderDetailInfo.get("TYPE").toString());
        allocInfo.setIdRequired(orderDetailInfo.get("IDREQUIRED").toString());
        allocInfo.setAllocationStrategyKey(orderDetailInfo.get("ALLOCATESTRATEGYKEY").toString());

        BigDecimal openQty = (BigDecimal)orderDetailInfo.get("OPENQTY");
        BigDecimal qtyPreAllocated = (BigDecimal)orderDetailInfo.get("QTYPREALLOCATED");
        BigDecimal qtyAllocated = (BigDecimal)orderDetailInfo.get("QTYALLOCATED");
        BigDecimal qtyPicked = (BigDecimal)orderDetailInfo.get("QTYPICKED");
        BigDecimal qtyToBeAllocate = openQty.subtract(qtyPreAllocated).subtract(qtyAllocated).subtract(qtyPicked);

        allocInfo.setQtyToBeAllocate(qtyToBeAllocate);

        return allocInfo;

    }


    /**
     * 按容器分配（用于正常库存和冻结库存的任务拣货）
     * @param allocInfo
     */
    private void allocateOrderLine(AllocInfo allocInfo){

        //获取拣货策略明细记录
        List<Map<String,String>> allocationStrategyDetailList = AllocationStrategy.findAllocStrategyDetailsByKey(allocInfo.getAllocationStrategyKey());

        //根据优先级使用每一行明细策略对订单行进行分配（明细策略中会指定拣货代码，如A01）
        for (Map<String, String> allocationStrategyDetail : allocationStrategyDetailList) {

            allocInfo.setCurrentAllocStrategyDetail(allocationStrategyDetail);

            String pickCode = allocationStrategyDetail.get("PICKCODE");

            AllocationExecutor allocationExecutor = EHContextHelper.getBean(WMSServiceNames.ALLOCATION_PICK_CODE_PREFIX + pickCode, AllocationExecutor.class);

            allocationExecutor.allocate(allocInfo);

            if(allocInfo.getResult().getAllocStatus() == AllocInfo.AllocStatus.fullyAllocated) break;

        }
    }
}
