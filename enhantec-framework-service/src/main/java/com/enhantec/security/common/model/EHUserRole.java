package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@TableName("EH_USER_ROLE")
public class EHUserRole {

    @TableId
    String id;
    String userId;
    String roleName;

}