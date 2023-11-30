package com.enhantec.wms.backend.core.outbound.allocations;

public interface AllocationExecutor {
    boolean allocate(AllocInfo allocInfo);
}
