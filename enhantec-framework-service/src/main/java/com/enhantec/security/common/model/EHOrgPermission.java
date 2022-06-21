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
 * @TableName eh_org_permission
 */
@TableName(value ="eh_org_permission")
@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EHOrgPermission extends EHBaseModel implements Serializable {

    private String orgId;

    private String permissionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}