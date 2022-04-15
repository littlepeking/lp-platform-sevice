package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@TableName("EH_ROLE_PERMISSION")
public class EHRolePermission {

    String roleName;
    String authority;

}
