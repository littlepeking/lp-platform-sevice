package com.enhantec.security.common.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.enhantec.security.core.enums.AuthType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDTO implements Serializable {

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
