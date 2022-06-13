package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.core.enums.AuthType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
@Service
public interface EHBaseService<T> extends IService<T> {

    public List<EHUser> findAll();

    public Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);

}
