package com.enhantec.security.core.properties;

import com.enhantec.security.core.validate.code.ValidateCodeFilter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "enhantec.security")
public class SecurityProperties {
    private WebProperties web = new WebProperties();

    private ValidationCodeProperties validationCode = new ValidationCodeProperties();

}
