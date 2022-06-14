package com.enhantec.security.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enhantec.common.exception.EHApplicationException;
import com.enhantec.security.common.dtos.PermissionDTO;
import com.enhantec.security.common.mapper.*;
import com.enhantec.security.common.model.*;
import com.enhantec.security.core.annotation.ReloadRoleHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
public interface EHPermissionService extends IService<EHPermission> {

    List<EHPermission> findAll();

    List<EHPermission> findByRoleId(String roleId);

    EHPermission createPermission(PermissionDTO permissionDTO);

    EHRole assignPermToRole(String roleId, List<String> permissions);

    EHRole revokePermFromRole(String roleId, List<String> permissionIds);

    Page<Map<String,Object>> getPageData(Page<Map<String,Object>> page, QueryWrapper qw);

    Page<Map<String, Object>> getRolePermissionPageData(Page<Map<String, Object>> page, QueryWrapper qw);

}
