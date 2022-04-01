package com.enhantec.security.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties("enhantec.security")
public class SecurityProperties {
    private WebProperties web = new WebProperties();

    private ValidationCodeProperties validationCode = new ValidationCodeProperties();

}
