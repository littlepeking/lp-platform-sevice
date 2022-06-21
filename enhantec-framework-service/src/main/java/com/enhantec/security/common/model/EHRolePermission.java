package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.enhantec.common.model.EHBaseModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @TableName eh_role_permission
 */
@TableName(value ="eh_role_permission")
@Data
@SuperBuilder
@With
@AllArgsConstructor
@NoArgsConstructor
public class EHRolePermission extends EHBaseModel implements Serializable {

    /**
     * 
     */
    private String roleId;

    /**
     * 
     */
    private String permissionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}