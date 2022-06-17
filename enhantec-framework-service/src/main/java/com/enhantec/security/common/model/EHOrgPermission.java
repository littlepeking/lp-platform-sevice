package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.*;

/**
 * 
 * @TableName eh_org_permission
 */
@TableName(value ="eh_org_permission")
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EHOrgPermission implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    /**
     * 
     */
    private String orgId;

    /**
     * 
     */
    private String permissionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}