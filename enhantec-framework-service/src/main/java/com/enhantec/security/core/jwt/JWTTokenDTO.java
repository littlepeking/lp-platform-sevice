package com.enhantec.security.core.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author JiJie.LianG
 * @date 2021-04-12 00:41
 */
@Data
@AllArgsConstructor
public class JWTTokenDTO {

    private String idToken;

    private String userName;
}
