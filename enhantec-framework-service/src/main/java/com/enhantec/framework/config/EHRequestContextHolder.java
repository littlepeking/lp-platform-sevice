/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.framework.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;

@RequestScope
@Component
@Data
public class EHRequestContextHolder {

    String orgId;
    String dataSource;
    //用于存储和传递request级别的自定义上下文变量
    HashMap<String,Object> data = new HashMap();

}
