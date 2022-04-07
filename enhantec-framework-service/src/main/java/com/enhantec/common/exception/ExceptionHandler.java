package com.enhantec.common.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionHandler implements ProblemHandling {

    @Override
    public boolean isCausalChainsEnabled() {
        return true; //debug时显示错误信息，生产环境设为FALSE
    }
}
