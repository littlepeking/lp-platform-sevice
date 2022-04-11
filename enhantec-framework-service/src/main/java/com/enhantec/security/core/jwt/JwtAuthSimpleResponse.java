package com.enhantec.security.core.jwt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtAuthSimpleResponse {

   private final Object content;

}
