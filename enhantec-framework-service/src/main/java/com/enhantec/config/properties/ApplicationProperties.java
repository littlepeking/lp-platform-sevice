package com.enhantec.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties("enhantec.common")
public class ApplicationProperties {

    private WebProperties web = new WebProperties();

    private ValidationCodeProperties validationCode = new ValidationCodeProperties();

    private final Security security = new Security();

    public static class Security {

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


