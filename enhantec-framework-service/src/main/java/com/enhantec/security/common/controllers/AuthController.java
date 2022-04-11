package com.enhantec.security.common.controllers;

import com.enhantec.security.common.dtos.AuthDto;
import com.enhantec.security.common.dtos.LoginDto;
import com.enhantec.security.common.services.EHUserDetailsService;
import com.enhantec.security.core.jwt.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/authorize")
public class AuthController {


    @Autowired
    EHUserDetailsService ehUserDetailsService;
    @Autowired
    JWTTokenProvider tokenProvider;

    @PostMapping("/register")
    public LoginDto register(@Valid @RequestBody LoginDto loginDto){
        return loginDto;
    }

    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Object getCurrentUser(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication(); or
        return authentication;
    }

    @GetMapping("/detail")
    public Object getUserDetail(@AuthenticationPrincipal UserDetails userDetails) {
        //return SecurityContextHolder.getContext().getAuthentication(); or
        return userDetails;
    }



}
