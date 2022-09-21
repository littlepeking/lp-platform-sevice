package com.enhantec.common.utils;

public interface DBConst {
    static String DS_MASTER = "master";
    //To choose a db schema for DB operation based on http header 'orgId'
    static String DS_CONTEXT_ORG = "#header.orgId";
    //To choose a db schema for DB operation based on current method parameter 'orgId'
    static String DS_PARAM_ORG = "#orgId";

}
