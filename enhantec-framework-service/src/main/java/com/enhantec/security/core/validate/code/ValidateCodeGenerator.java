package com.enhantec.security.core.validate.code;

import javax.servlet.http.HttpServletRequest;

public interface ValidateCodeGenerator {
    public ImageCode generateImageCode(HttpServletRequest request);
}
