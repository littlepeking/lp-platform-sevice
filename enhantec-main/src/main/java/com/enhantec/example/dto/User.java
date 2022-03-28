package com.enhantec.example.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Data
public class User {


    int userId;

    @NotBlank
    String userName;

    @NotNull
    LocalDateTime testDate;
}
