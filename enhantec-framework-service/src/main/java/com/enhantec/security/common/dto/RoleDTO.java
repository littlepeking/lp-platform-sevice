package com.enhantec.security.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RoleDTO {

    String id;

    @NotNull
    String orgId;

    @NotNull
    String roleName;

    @NotNull
    String displayName;

}
