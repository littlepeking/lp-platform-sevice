package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dtos.UserRegisterDTO;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.model.TestReceipt;
import com.enhantec.security.common.service.EHUserDetailsService;
import com.enhantec.security.common.service.EHUserService;
import com.enhantec.security.common.service.TestReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/api/security/user")
@RequiredArgsConstructor
public class UserController {

    //TODO: Integrate will Redis to make JWT token expired

    private final EHUserService ehUserService;

    private final EHUserDetailsService ehUserDetailsService;

    @GetMapping("/userInfo")
    //@PreAuthorize("hasAuthority('USER_READ')")
    public Object getCurrentUser(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication();
        return ehUserDetailsService.getUserInfo(authentication.getName());
    }

    @GetMapping("/getAllUsers")
    public List<EHUser> getAllUsers(){
        return ehUserService.findAll();
    }

    @PostMapping("/createUser")
    public EHUser createUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO){
        return ehUserService.createUser(userRegisterDTO.getUsername(),userRegisterDTO.getPassword(),userRegisterDTO.getAuth_type());
    }

    @PostMapping("/queryByPage")
    public Page<Map<String,Object>> queryByPage(@RequestBody PageParams pageParams){

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String,Object>> res = ehUserService.getPageData(pageInfo,queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}
