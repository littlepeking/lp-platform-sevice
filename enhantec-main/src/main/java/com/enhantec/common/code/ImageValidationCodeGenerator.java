package com.enhantec.common.code;

import com.enhantec.security.core.validate.code.ImageCode;
import com.enhantec.security.core.validate.code.ValidateCodeGenerator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

//@Component("imageCodeGenerator")  --comment this as we do not need do customization for now, use default function in core jar
public class ImageValidationCodeGenerator implements ValidateCodeGenerator {
    @Override
    public ImageCode generateImageCode(HttpServletRequest request) {
        //EH customized image validation code
        return null;
    }
}
