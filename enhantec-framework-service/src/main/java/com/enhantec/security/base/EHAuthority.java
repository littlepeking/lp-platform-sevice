package com.enhantec.security.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;


@Data
@AllArgsConstructor
public class EHAuthority implements GrantedAuthority, Serializable {

    private String authority;


}
