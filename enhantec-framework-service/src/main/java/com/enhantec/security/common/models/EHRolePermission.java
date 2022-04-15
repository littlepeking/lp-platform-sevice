package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@TableName("EH_ROLE_PERMISSION")
public class EHRolePermission {
    @TableId
    String id;
    String roleName;
    String authority;

}
