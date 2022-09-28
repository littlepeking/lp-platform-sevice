/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.security.common.service.impl;

import com.enhantec.security.common.service.JWTCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.enhantec.security.core.CacheConfig.JWT_TOKEN_CACHE_NAME;
import static com.enhantec.security.core.CacheConfig.USER_TOKEN_CACHE_NAME;

@Service
public class JWTCacheServiceImpl implements JWTCacheService {

    @CachePut(cacheNames = JWT_TOKEN_CACHE_NAME, key = "#jwt")
    public String addOrRenewToken(String jwt,String ipAddress) {
        return ipAddress;
    }

    @CachePut(cacheNames = USER_TOKEN_CACHE_NAME, key = "#userId")
    public String addOrRenewUserToken(String userId, String jwt) {
        return jwt;
    }

    @Cacheable(value = JWT_TOKEN_CACHE_NAME, unless = "#result == null")
    public String getToken(String jwt) {
        return null;
    }

    @Cacheable(value = USER_TOKEN_CACHE_NAME, unless = "#result == null")
    public String getUserToken(String userId) {
        return null;
    }

    @CacheEvict(value = JWT_TOKEN_CACHE_NAME, key = "#cacheKey")
    public void removeToken(String cacheKey) {}

    @CacheEvict(value = USER_TOKEN_CACHE_NAME, key = "#cacheKey")
    public void removeUserToken(String cacheKey) {}

}
