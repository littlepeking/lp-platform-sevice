package com.enhantec.security.common.dto;

import com.enhantec.common.dto.VersionDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RoleDTO extends VersionDTO {

    String id;

    @NotNull
    String orgId;

    @NotNull
    String roleName;

    @NotNull
    String displayName;

}
