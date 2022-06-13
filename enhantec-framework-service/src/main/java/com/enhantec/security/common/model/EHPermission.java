package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
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

    @TableId
    @NotNull
    private String id;

    @NotNull
    private String orgId;// ONLY system tables has orgId as all biz transaction table rely on schema name to difference org.

    @NotNull
    private String authority;

    private String displayName;

}
