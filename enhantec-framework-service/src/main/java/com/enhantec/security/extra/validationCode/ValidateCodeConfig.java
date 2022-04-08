package com.enhantec.security.extra.validationCode;

import com.enhantec.config.properties.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidateCodeConfig {

    @Autowired
    private ApplicationProperties applicationProperties;


    @Bean
    //@ConditionalOnMissingBean(name="imageCodeGenerator")
    public ValidateCodeGenerator imageCodeGenerator(){
        ImageValidateCodeGenerator codeGenerator = new ImageValidateCodeGenerator();
        codeGenerator.setApplicationProperties(applicationProperties);
        return codeGenerator;
    }

}
