package com.enhantec.security.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginDto implements Serializable {

    @NotNull
    private String username;
    @NotNull
    private String password;

//    @Past(message = "testDate 必须是过去的时间")
//    LocalDateTime testDate;

}
