package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.security.common.model.EHUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
public interface EHUserService extends IService<EHUser> {

    List<EHUser> findAll();

    EHUser createOrUpdate(EHUser user);

    void enable(String userId);

    void disable(String userId);

    void delete(String userId);

    void checkIfUsernameExists(String username);

    Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);




}
