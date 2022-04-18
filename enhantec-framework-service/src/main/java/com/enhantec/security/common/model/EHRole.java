package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("EH_ROLE")
public class EHRole implements GrantedAuthority, Serializable {

    @TableId(type = IdType.INPUT)
    @NotNull
    private String roleName;

    @NotNull
    private String displayName;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return roleName;
    }


    @TableField(exist = false)
    private Collection<EHPermission> permissions;


}
