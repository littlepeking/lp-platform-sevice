package com.enhantec.security.common.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.mapper.EHBaseMapper;
import com.enhantec.security.common.model.EHPermission;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface EHPermissionMapper extends EHBaseMapper<EHPermission> {

    @MapKey("id")
    Page<Map<String, Object>> queryRolePermissionPageData(@Param("page") Page<Map<String, Object>> page, @Param("ew") QueryWrapper qw);
}
