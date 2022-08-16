package com.enhantec.common.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class PageParams {

    public int getPageNum(){
        return pageIndex+1;
    }

    int pageSize = 25;
    int pageIndex;

    List<EHDataGridFilterInfo> filters;
    HashMap<String,Object> params;
    HashMap<String,String> orderBy;
}
