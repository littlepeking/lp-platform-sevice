/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.wms.backend.utils.common;

public class FulfillLogicException extends RuntimeException {

    public FulfillLogicException() {
    }

    public FulfillLogicException(String str) {
        super(str);
    }

    public FulfillLogicException(String str,Object ...params) {
        super(str);
    }

    public FulfillLogicException(Throwable cause) {
        super(cause);
    }

    public FulfillLogicException(String str, Throwable cause) {
        super(str, cause);
    }

}
