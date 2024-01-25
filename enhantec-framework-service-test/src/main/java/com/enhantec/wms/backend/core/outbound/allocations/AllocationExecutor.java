package com.enhantec.wms.backend.core.outbound.allocations;

public interface AllocationExecutor {
    void allocate(OrderDetailAllocInfo orderDetailAllocInfo);
}
