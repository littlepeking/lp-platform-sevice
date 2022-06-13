package com.enhantec.security.common.mapper;
import java.util.List;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enhantec.security.common.model.EHUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EHUserMapper extends BaseMapper<EHUser> {

    List<EHUser> selectAllById(@Param("id") String id);

}
