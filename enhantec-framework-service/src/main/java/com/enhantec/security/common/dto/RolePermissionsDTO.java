package com.enhantec.security.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionsDTO implements Serializable {

    @NotNull
    private String roleId;

    private List<String> permissionIds;
}