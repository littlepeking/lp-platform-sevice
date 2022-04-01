package com.enhantec.example.test.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Data
public class User {

    public interface UserSubView1{};
    public interface UserSubView2{};


    @JsonView(UserSubView2.class)
    int userId;

    @NotBlank(message = "userName不能为空")
    String userName;

    @NotNull
    @JsonView(UserSubView1.class)
     @Past(message = "testDate 必须是过去的时间")
    LocalDateTime testDate;
}
