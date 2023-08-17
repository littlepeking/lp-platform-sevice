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
