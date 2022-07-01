package com.enhantec.security.core.jwt;

import com.enhantec.common.utils.EHLocaleHelper;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthException extends AuthenticationException {

    public JwtAuthException(String msgKey){
        super(EHLocaleHelper.getMsg(msgKey, null));
    }

    public JwtAuthException(String msgKey, String... args) {
        super(EHLocaleHelper.getMsg(msgKey, args));
    }
}
