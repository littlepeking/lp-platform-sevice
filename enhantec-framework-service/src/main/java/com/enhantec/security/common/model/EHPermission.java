package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHVersionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_PERMISSION")
public class EHPermission extends EHVersionModel implements GrantedAuthority, Serializable {

    @NotNull
    private String parentId;

    /**
     * D: Directory
     * P: Permission
     */
    @NotNull
    private String type;

    @NotNull
    private String authority;

    private String displayName;

    /**
     * Used for org perm selection display
     * 0: Not select
     * 1: Selected
     * 2: Semi-selected
     */
    @TableField(exist = false)
    private Boolean checkStatus;

    @TableField(exist = false)
    private List<EHPermission> children; //only use when type is P

}
