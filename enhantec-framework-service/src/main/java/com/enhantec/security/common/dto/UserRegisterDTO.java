package com.enhantec.security.common.dto;

import com.enhantec.security.core.enums.AuthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegisterDTO implements Serializable {

    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private AuthType auth_type;

//    @Past(message = "testDate 必须是过去的时间")
//    LocalDateTime testDate;

}
