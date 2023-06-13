/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.core.auth;

import com.enhantec.framework.common.utils.EHLocaleHelper;
import org.springframework.security.core.AuthenticationException;

public class EHAuthException extends AuthenticationException {

    public EHAuthException(String msgKey){
        super(EHLocaleHelper.getMsg(msgKey, null));
    }

    public EHAuthException(String msgKey, String... args) {
        super(EHLocaleHelper.getMsg(msgKey, args));
    }
}
