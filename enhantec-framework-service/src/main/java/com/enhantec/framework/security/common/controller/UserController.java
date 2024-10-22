/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.security.common.dto.UserDTO;
import com.enhantec.framework.security.common.dto.UserPasswordChangeDTO;
import com.enhantec.framework.security.common.model.EHUser;
import com.enhantec.framework.security.common.service.EHUserDetailsService;
import com.enhantec.framework.security.common.service.EHUserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security/user")
@RequiredArgsConstructor
public class UserController {

    //TODO: Integrate will Redis to make JWT token expired

    private final EHUserService ehUserService;

    private final EHUserDetailsService ehUserDetailsService;

    @PreAuthorize("permitAll()")
    @GetMapping("/userInfo")
    //@PreAuthorize("hasAuthority('USER_READ')")
    public Object getCurrentUser(Authentication authentication) {
        //return SecurityContextHolder.getContext().getAuthentication();
        return ehUserDetailsService.getUserInfo(authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @GetMapping("/findById/{id}")
    public EHUser findById(@NotNull @PathVariable String id){
        return ehUserService.getById(id);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @GetMapping("/findAll")
    public List<EHUser> findAll(){
        return ehUserService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/createOrUpdate")
    public EHUser createOrUpdate(@Valid @RequestBody UserDTO userDTO){

        EHUser user = EHUser.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .authType(userDTO.getAuthType())
                .accountLocked(userDTO.isAccountLocked())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .originalPassword(userDTO.getOriginalPassword())
                .password(userDTO.getPassword())
                .apiKey(userDTO.getApiKey())
                .remark(userDTO.getRemark())
                .version(userDTO.getVersion())
                .build();

        return ehUserService.createOrUpdate(user);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @DeleteMapping("")
    public void delete(@RequestBody @NotNull List<String> ids) {

        ids.forEach(id-> ehUserService.delete(id));
       ;
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/changePassword")
    public void changePassword(@Valid @RequestBody UserPasswordChangeDTO userPasswordChangeDTO) {

        ehUserService.changePassword(userPasswordChangeDTO.getUsername(), userPasswordChangeDTO.getOriginalPassword(), userPasswordChangeDTO.getNewPassword());

    }


    @PostMapping("/disable/{userId}")
    public void disable(@NotNull @PathVariable String userId) {
        ehUserService.disable(userId);
    }

    @PostMapping("/enable/{userId}")
    public void enable(@NotNull @PathVariable String userId) {
        ehUserService.enable(userId);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_USER')")
    @PostMapping("/queryByPage")
    public Page<Map<String,Object>> queryByPage(@RequestBody PageParams pageParams){

        Page<Map<String, Object>> pageInfo = EHPaginationHelper.buildPageInfo(pageParams);

        val queryWrapper = EHPaginationHelper.buildQueryWrapperByPageParams(pageParams);

        Page<Map<String,Object>> res = ehUserService.getPageData(pageInfo,queryWrapper);

        //DataFormatHelper.formatPageData(res);

        return res;

    }


}
