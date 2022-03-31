package com.enhantec.common.controllers;

import com.enhantec.example.test.dto.User;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/info")
    public Object getCurrentUser(Authentication authentication){
        //return SecurityContextHolder.getContext().getAuthentication(); or
        return authentication;
    }

    @GetMapping("/detail")
    public Object getUserDetail(@AuthenticationPrincipal UserDetails userDetails){
        //return SecurityContextHolder.getContext().getAuthentication(); or
        return userDetails;
    }


    @JsonView(User.UserSubView1.class)
    @PostMapping("/add")
    public User addUser(@Valid @RequestBody User user
                        //  , BindingResult result
    ){

//        if(result.hasErrors()){
//            System.out.println(result);
//         //   throw new Exception(result.toString());
//        }
        user.setUserId(1);
        return user;
    }

}
