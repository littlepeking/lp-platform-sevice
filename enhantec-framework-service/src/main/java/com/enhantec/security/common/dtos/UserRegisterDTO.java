package com.enhantec.security.common.dtos;

import com.enhantec.security.core.enums.AuthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.LowerCase;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.time.LocalDateTime;

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
