package com.enhantec.common.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class PageParams {
    int pageNum = 1;
    int pageSize = 25;
    HashMap<String,Object> filters;
    HashMap<String,Object> extraParams;
    HashMap<String,String> orderBy;
}
