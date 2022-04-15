package com.enhantec.security.common.models;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@TableName("EH_ROLE")
public class EHRole implements GrantedAuthority, Serializable {

    @NotNull
    private String roleName;

    @NotNull
    private String displayName;

    @Override
    public String getAuthority() {
        return roleName;
    }
}
