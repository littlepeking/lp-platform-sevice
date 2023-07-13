/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

package com.enhantec.wms.backend.utils.common;

public class DBResourceException extends RuntimeException{

    public int nativeCode;
    public int reasonCode;

    public DBResourceException() {
    }

    public DBResourceException(String str) {
        super(str);
    }

    public DBResourceException(Throwable cause) {
        super(cause);
    }

    public DBResourceException(String str, Throwable cause) {
        super(str, cause);
    }

}
