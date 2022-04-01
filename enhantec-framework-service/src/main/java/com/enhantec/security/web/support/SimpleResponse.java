package com.enhantec.security.web.support;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SimpleResponse {

   private final Object content;

}
