package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@With
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"roles"})
@EqualsAndHashCode(exclude = {"roles"})
@TableName("EH_PERMISSION")
public class EHPermission implements GrantedAuthority, Serializable {

    private static final long serialVersionUid =1L;

    @TableId
    @NotNull
    private String authority;

    private String displayName;

}
