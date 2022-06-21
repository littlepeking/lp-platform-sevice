package com.enhantec.common.model;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EHBaseModel {

    @TableId
    String id;

}
