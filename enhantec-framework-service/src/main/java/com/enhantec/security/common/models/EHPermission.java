package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.management.relation.Role;
import javax.validation.constraints.NotNull;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Set;

@With
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"roles"})
@EqualsAndHashCode(exclude = {"roles"})
@TableName("EH_PERMISSION")
public class EHPermission implements GrantedAuthority, Serializable {

    private static  final long serialVersionUid =1L;

    @NotNull
    private String authority;

    private String displayName;

}
