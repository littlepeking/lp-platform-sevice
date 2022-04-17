package com.enhantec.security.common.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RoleDTO {

    @NotNull
    String roleName;
    @NotNull
    String displayName;

}
