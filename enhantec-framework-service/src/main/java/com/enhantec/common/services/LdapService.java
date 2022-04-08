package com.enhantec.common.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class LdapService {

 //   public JWTUser authenticate(String userName) {
        //get user from ldap service

//        String domainUserName = null;
//        if (userName != null) {
//            if (userName.contains("cn=")) {
//                // infor_v11.4.1 user_name 包含域,临时过滤;
//                // eg: cn=liang,ou=wms,dc=sce,dc=com
//                String[] split = StringUtils.split(userName, ",", 0);
//                if (split.length > 0) {
//                    domainUserName = split[0];
//                    domainUserName = domainUserName.replace("cn=", "");
//                }
//            }
//
//
//        }
//
//        JWTUser user = new JWTUser("cn=lp,dn=infor", "123", new ArrayList<GrantedAuthority>(), domainUserName);
//
//        return user;
//    }
}
