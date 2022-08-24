package com.enhantec.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@With
@SuperBuilder(builderMethodName = "EHTreeModelBuilder")
@NoArgsConstructor
@AllArgsConstructor
public class EHTreeModel<T extends EHTreeModel> extends EHVersionModel {


    @NotNull
    private String parentId;

    @TableField(exist = false)
    private Boolean checkStatus;

    @TableField(exist = false)
    private List<T> children;

}
