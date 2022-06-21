package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHBaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;


@Data
@SuperBuilder
@With
@AllArgsConstructor
@NoArgsConstructor
@TableName("EH_ROLE")
public class EHRole extends EHBaseModel implements GrantedAuthority, Serializable {

    @NotNull
    private String orgId;// ONLY system tables has orgId as all biz transaction table rely on schema name to difference org.

    @NotNull
    private String roleName;

    @NotNull
    private String displayName;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return super.getId();
    }


    @TableField(exist = false)
    private Collection<EHPermission> permissions;


}
