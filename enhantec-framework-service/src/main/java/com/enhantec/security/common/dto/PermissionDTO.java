package com.enhantec.security.common.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionDTO {

    String id;

    @NotNull
    String parentId;

    @NotNull
    private String type;

    String authority;

    @NotNull
    String displayName;

}
