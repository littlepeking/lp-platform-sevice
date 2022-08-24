package com.enhantec.security.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.enhantec.common.model.EHTreeModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
public class EHOrganization extends EHTreeModel<EHOrganization> implements Serializable {

    private String code;
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

}