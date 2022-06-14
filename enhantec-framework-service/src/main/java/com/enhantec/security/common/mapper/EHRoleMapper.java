package com.enhantec.security.common.mapper;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.security.common.model.EHRole;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface EHRoleMapper extends BaseMapper<EHRole> {

    @MapKey("id")
    Page<Map<String, Object>> queryUserRolePageData(@Param("page") Page<Map<String, Object>> page, @Param("ew") QueryWrapper<EHRole> qw);

}
