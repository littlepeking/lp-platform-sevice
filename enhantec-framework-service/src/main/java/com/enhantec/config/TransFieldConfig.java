package com.enhantec.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransFieldConfig {

    private String transIdFieldName;//翻译所依据的 ID - TransId 在目标对象存储的Field
    private String transTextFieldName;//翻译后写入目标对象的Field
    private String transTableName;//在翻译表中需要检索的表名
    private String transColumnName;//在翻译表中需要检索的列名

}
