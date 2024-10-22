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



package com.enhantec.framework.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties("enhantec.common")
public class ApplicationProperties {

    private WebProperties web = new WebProperties();

    private ValidationCodeProperties validationCode = new ValidationCodeProperties();

    private final Security security = new Security();

    public static class Security {

        @Getter
        @Setter
        private long passwordExpiredDays = 30;
        //basic,ldap
        private List<String> authTypes = Collections.singletonList("BASIC");

        public List<String> getAuthTypes() {
            return authTypes;
        }

        public void setAuthTypes(List<String> authTypes) {
                this.authTypes = authTypes.stream().map(e-> StringUtils.upperCase(e.trim())).collect(Collectors.toList());

        }

        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }

        public static class Jwt {
            private String base64Secret = null;

            private long tokenValidityInSeconds = 1800; // 30 minutes

            public String getBase64Secret() {
                return base64Secret;
            }

            public void setBase64Secret(String base64Secret) {
                this.base64Secret = base64Secret;
            }

            public long getTokenValidityInSeconds() {
                return tokenValidityInSeconds;
            }

            public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
                this.tokenValidityInSeconds = tokenValidityInSeconds;
            }

        }
    }

}


