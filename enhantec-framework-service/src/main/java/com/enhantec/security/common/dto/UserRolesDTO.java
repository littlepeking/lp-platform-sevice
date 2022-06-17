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
public class UserRolesDTO implements Serializable {

    @NotNull
    private String userId;

    private List<String> roleIds;
}
