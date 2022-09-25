package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHVersionModel;
import com.enhantec.config.annotations.TransField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
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
public class EHRole extends EHVersionModel implements GrantedAuthority, Serializable {

    @NotNull
    private String orgId;// ONLY system tables has orgId as all biz transaction table rely on schema name to difference org.

    @NotNull
    private String roleName;

    @NotNull
    @TransField
    private String displayName;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return super.getId();
    }


    @TableField(exist = false)
    private Collection<EHPermission> permissions;


}
