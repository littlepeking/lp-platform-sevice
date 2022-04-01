package com.enhantec.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(EHApplicationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleEHApplicationException(EHApplicationException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("errMsg", ex.getMessage());
        return res;
    }

}
