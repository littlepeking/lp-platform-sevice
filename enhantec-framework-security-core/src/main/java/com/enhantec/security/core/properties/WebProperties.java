package com.enhantec.security.core.properties;

import lombok.Data;

@Data
public class WebProperties {

   private String loginPage = "/web-common-login.html";

   LoginType loginType = LoginType.JSON;

}
