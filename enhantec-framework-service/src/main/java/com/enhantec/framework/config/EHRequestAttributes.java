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

package com.enhantec.framework.config;

import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;
import java.util.HashMap;

//This class maintains a map of request scope beans internally. if it contains the bean then it returns that bean otherwise it returns null.
public class EHRequestAttributes implements RequestAttributes {
    private Map<String, Object> requestAttributeMap = new HashMap<>();
    @Override
    public Object getAttribute(String name, int scope) {
        if(scope == RequestAttributes.SCOPE_REQUEST) {
            return this.requestAttributeMap.get(name);
        }
        return null;
    }
    @Override
    public void setAttribute(String name, Object value, int scope) {
        if(scope == RequestAttributes.SCOPE_REQUEST){
            this.requestAttributeMap.put(name, value);
        }
    }
    @Override
    public void removeAttribute(String name, int scope) {
        if(scope == RequestAttributes.SCOPE_REQUEST) {
            this.requestAttributeMap.remove(name);
        }
    }
    @Override
    public String[] getAttributeNames(int scope) {
        if(scope == RequestAttributes.SCOPE_REQUEST) {
            return this.requestAttributeMap.keySet().toArray(new String[0]);
        }
        return  new String[0];
    }
    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        // Not Supported
    }
    @Override
    public Object resolveReference(String key) {
        // Not supported
        return null;
    }
    @Override
    public String getSessionId() {
        return null;
    }
    @Override
    public Object getSessionMutex() {
        return null;
    }
}