package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dto.UserDTO;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import com.enhantec.security.common.service.EHUserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

    @GetMapping("/findById/{id}")
    public EHUser findById(@NotNull @PathVariable String id){
        return ehUserService.getById(id);
    }


    @GetMapping("/findAll")
    public List<EHUser> findAll(){
        return ehUserService.findAll();
    }

    @PostMapping("/createOrUpdate")
    public EHUser createOrUpdate(@Valid @RequestBody UserDTO userDTO){

        EHUser user = EHUser.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .authType(userDTO.getAuthType())
                .accountLocked(userDTO.isAccountLocked())
                .credentialsExpired(userDTO.isCredentialsExpired())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .originalPassword(userDTO.getOriginalPassword())
                .password(userDTO.getPassword())
                .version(userDTO.getVersion())
                .build();

        return ehUserService.createOrUpdate(user);
    }


    @DeleteMapping("")
    public void delete(@RequestParam @NotNull List<String> userIds) {

        userIds.forEach(id-> ehUserService.delete(id));
       ;
    }


    @PostMapping("/disable/{userId}")
    public void disable(@NotNull @PathVariable String userId) {
        ehUserService.disable(userId);
    }

    @PostMapping("/enable/{userId}")
    public void enable(@NotNull @PathVariable String userId) {
        ehUserService.enable(userId);
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
