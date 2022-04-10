package com.enhantec.security.core.authentication;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestAuthSimpleResponse {

   private final Object content;

}
