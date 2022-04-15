package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@TableName("EH_USER_ROLE")
public class EHUserRole {

    String username;
    String roleName;

}
