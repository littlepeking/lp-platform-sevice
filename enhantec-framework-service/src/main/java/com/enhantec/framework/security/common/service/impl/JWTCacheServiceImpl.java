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



package com.enhantec.framework.security.common.service.impl;

import com.enhantec.framework.security.common.service.JWTCacheService;
import com.enhantec.framework.security.core.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class JWTCacheServiceImpl implements JWTCacheService {

    @CachePut(cacheNames = CacheConfig.JWT_TOKEN_CACHE_NAME, key = "#jwt")
    public String addOrRenewToken(String jwt,String ipAddress) {
        return ipAddress;
    }

    @CachePut(cacheNames = CacheConfig.USER_TOKEN_CACHE_NAME, key = "#userId")
    public String addOrRenewUserToken(String userId, String jwt) {
        return jwt;
    }

    @Cacheable(value = CacheConfig.JWT_TOKEN_CACHE_NAME, unless = "#result == null")
    public String getToken(String jwt) {
        return null;
    }

    @Cacheable(value = CacheConfig.USER_TOKEN_CACHE_NAME, unless = "#result == null")
    public String getUserToken(String userId) {
        return null;
    }

    @CacheEvict(value = CacheConfig.JWT_TOKEN_CACHE_NAME, key = "#cacheKey")
    public void removeToken(String cacheKey) {}

    @CacheEvict(value = CacheConfig.USER_TOKEN_CACHE_NAME, key = "#cacheKey")
    public void removeUserToken(String cacheKey) {}

}
