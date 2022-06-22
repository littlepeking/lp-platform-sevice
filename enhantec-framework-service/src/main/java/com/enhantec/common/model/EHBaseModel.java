package com.enhantec.common.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EHBaseModel {

    @TableId
    String id;

    @TableField(fill = FieldFill.INSERT)
    LocalDateTime addDate;

    @TableField(fill = FieldFill.INSERT)
    String addWho;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    LocalDateTime editDate;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    String editWho;

}
