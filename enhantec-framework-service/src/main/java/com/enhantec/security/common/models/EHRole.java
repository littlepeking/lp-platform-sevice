package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@TableName("EH_ROLE")
public class EHRole implements GrantedAuthority, Serializable {

    @TableId
    @NotNull
    private String roleName;

    @NotNull
    private String displayName;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return roleName;
    }
}
