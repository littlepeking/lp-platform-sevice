package com.enhantec.common.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class PageParams {
    int pageNum = 1;
    int pageSize = 25;
    List<EHDataGridFilterInfo> filters;
    HashMap<String,Object> extraParams;
    HashMap<String,String> orderBy;
}
