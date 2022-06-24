package com.enhantec.security.common.dto;

import com.enhantec.common.dto.VersionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO extends VersionDTO implements Serializable {

    private String id;
    @NotNull
    private String code;
    @NotNull
    private String parentId;
    @NotNull
    private String name;

    private String address1;

    private String address2;

    private String contact1;

    private String contact2;


}
