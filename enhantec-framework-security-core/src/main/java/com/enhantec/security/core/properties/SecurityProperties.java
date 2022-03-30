package com.enhantec.security.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "enhantec.security")
public class SecurityProperties {
    private WebProperties webProperties = new WebProperties();

}
