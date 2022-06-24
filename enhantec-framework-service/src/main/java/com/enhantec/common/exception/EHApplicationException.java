package com.enhantec.common.exception;

import com.enhantec.common.utils.EHLocaleHelper;

public class EHApplicationException extends RuntimeException{

    public EHApplicationException(String msgKey){
        super(EHLocaleHelper.getMsg(msgKey, null));
    }

    public EHApplicationException(String msgKey, String... args) {
        super(EHLocaleHelper.getMsg(msgKey, args));
    }
}
