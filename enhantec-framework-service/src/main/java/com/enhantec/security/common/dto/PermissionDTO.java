package com.enhantec.security.common.dto;

import com.enhantec.common.dto.VersionDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionDTO extends VersionDTO {

    String id;

    String parentId;

    @NotNull
    private String type;

    String authority;

    @NotNull
    String displayName;

}
