package com.enhantec.security.common.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionDTO {

    @NotNull
    String permissionName;
    @NotNull
    String displayName;

}
