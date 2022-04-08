package com.enhantec.common.controllers;

import com.enhantec.example.test.dto.User;
import com.enhantec.security.jwt.JWTFilter;
import com.enhantec.security.jwt.JWTTokenDTO;
import com.enhantec.security.jwt.JWTTokenProvider;
import com.enhantec.security.web.EHUserDetailsService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    EHUserDetailsService ehUserDetailsService;
    @Autowired
    JWTTokenProvider tokenProvider;

//
//    @GetMapping("/authenticate")
//    public ResponseEntity<JWTTokenDTO> authenticate(String userName) {
//
//
//        if (userName.contains("cn=")) {
//            // infor_v11.4.1 user_name 包含域,临时过滤;
//            // eg: cn=liang,ou=wms,dc=sce,dc=com
//            String[] split = StringUtils.split(userName, ",", 0);
//            if (split.length > 0) {
//                userName = split[0];
//                userName = userName.replace("cn=", "");
//            }
//        }
//        JWTUser userDetails = (JWTUser) ehUserDetailsService.loadUserByUsername(userName);
//
//        // Security
//        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // custom payload
//        HashMap<String, Object> extPayLoad = new HashMap<>();
//        // jwt token
//        String jwt = tokenProvider.createToken(authentication, extPayLoad);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
//
//        JWTTokenDTO jwtTokenDTO = new JWTTokenDTO(jwt, userDetails.getUsername());
//
//        return new ResponseEntity<>(jwtTokenDTO, httpHeaders, HttpStatus.OK);
//    }

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


    @JsonView(User.UserSubView1.class)
    @PostMapping("/add")
    public User addUser(@Valid @RequestBody User user
                        //  , BindingResult result
    ) {

//        if(result.hasErrors()){
//            System.out.println(result);
//         //   throw new Exception(result.toString());
//        }
        user.setUserId(1);
        return user;
    }

}
