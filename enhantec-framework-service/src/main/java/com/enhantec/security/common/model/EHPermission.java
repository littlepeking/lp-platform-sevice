package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHTreeModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_PERMISSION")
public class EHPermission extends EHTreeModel<EHPermission> implements GrantedAuthority, Serializable {
    /**
     * D: Directory
     * P: Permission
     */
    @NotNull
    private String type;

    @NotNull
    private String authority;

    private String moduleId;

    private String displayName;


}
