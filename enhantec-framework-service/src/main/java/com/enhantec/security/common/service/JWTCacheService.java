package com.enhantec.security.common.service;

import org.springframework.stereotype.Service;

@Service
public interface JWTCacheService {

    String addOrRenewToken(String jwt,String ipAddress);

    String addOrRenewUserToken(String userId, String jwt);

    String getToken(String jwt);

    String getUserToken(String jwt);

    void removeToken(String cacheKey);

    void removeUserToken(String cacheKey);

}
