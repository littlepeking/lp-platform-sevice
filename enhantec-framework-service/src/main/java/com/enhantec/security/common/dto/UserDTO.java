package com.enhantec.security.common.dto;

import com.enhantec.common.dto.VersionDTO;
import com.enhantec.security.core.enums.AuthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO extends VersionDTO implements Serializable {

    private String id;
    @NotNull
    private String username;

    private String originalPassword;

    private String password;
    @NotNull
    private AuthType authType;

    private String firstName;

    private String lastName;

    private boolean accountLocked;

    private boolean credentialsExpired;
}
