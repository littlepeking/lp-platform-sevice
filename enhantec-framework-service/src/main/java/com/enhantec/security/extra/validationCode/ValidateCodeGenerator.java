package com.enhantec.security.extra.validationCode;

import javax.servlet.http.HttpServletRequest;

public interface ValidateCodeGenerator {
    public ImageCode generateImageCode(HttpServletRequest request);
}
