package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("EH_PERMISSION")
public class EHPermission implements GrantedAuthority, Serializable {

    @TableId
    @NotNull
    private String id;

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
    private int checkStatus;

    @TableField(exist = false)
    private List<EHPermission> children; //only use when type is P

}
