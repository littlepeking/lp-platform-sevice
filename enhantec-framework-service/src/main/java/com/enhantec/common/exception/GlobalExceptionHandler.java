package com.enhantec.common.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EHApplicationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleEHApplicationException(HttpServletRequest request, HttpServletResponse response, EHApplicationException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("error", ex.getMessage());
        return res;
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("error", ex.getMessage());
        return res;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("error", ex.getMessage());
        return res;
    }



//    //securityProblemSupport did not triggered for access denied exception, so deal with it here before the issue can be figured out.
//    //https://stackoverflow.com/questions/43554489/spring-mvc-accessdeniedexception-500-error-received-instead-of-custom-401-error
//    @org.springframework.web.bind.annotation.ExceptionHandler(value = {AccessDeniedException.class})
//    public void commence(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
//        Map<String, Object> res = new HashMap<>();
//        res.put("error", ex.getMessage());
//        return res;
//    }

//    @org.springframework.web.bind.annotation.ExceptionHandler(value = {AuthenticationException.class})
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.getOutputStream().println("{ \"error\": \"" + authException.getMessage() + "\" }");
//    }

}