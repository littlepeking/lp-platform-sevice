package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.*;

import com.enhantec.common.model.EHBaseModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * 
 * @TableName eh_organization
 */
@TableName(value ="eh_organization")
@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EHOrganization extends EHBaseModel implements Serializable {
    /**
     * 
     */
    @TableId
    private String id;

    private String code;

    /**
     * 
     */
    @NotNull
    private String parentId;

    /**
     * 
     */
    @NotNull
    private String name;

    /**
     * 
     */
    private String address1;

    /**
     * 
     */
    private String address2;

    /**
     * 
     */
    private String contact1;

    /**
     * 
     */
    private String contact2;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @TableField(exist = false)
    private List<EHOrganization> children;
}