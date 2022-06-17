package com.enhantec.security.common.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.common.model.PageParams;
import com.enhantec.common.utils.EHPaginationHelper;
import com.enhantec.security.common.dto.OrganizationDTO;
import com.enhantec.security.common.dto.RoleDTO;
import com.enhantec.security.common.dto.UserRolesDTO;
import com.enhantec.security.common.model.EHOrganization;
import com.enhantec.security.common.model.EHPermission;
import com.enhantec.security.common.model.EHRole;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHOrganizationService;
import com.enhantec.security.common.service.EHRoleService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/api/security/role")
@RequiredArgsConstructor
public class OrganizationController {

    private final EHOrganizationService ehOrganizationService;

    @GetMapping("/getAll")
    public List<EHOrganization> getAll() {
        return ehOrganizationService.list();
    }

    @GetMapping("/getOrgTree")
    public EHOrganization getOrgTree(){
        return ehOrganizationService.buildOrgTree();
    }

    @PostMapping("/createOrUpdate")
    public EHOrganization createOrUpdate(@Valid @RequestBody OrganizationDTO organizationDTO) {

        EHOrganization organization = EHOrganization.builder()
                .id(organizationDTO.getId())
                .name(organizationDTO.getName())
                .code(organizationDTO.getCode())
                .address1(organizationDTO.getAddress1())
                .address2(organizationDTO.getAddress2())
                .contact1(organizationDTO.getContact1())
                .contact2(organizationDTO.getContact2()).build();

        ehOrganizationService.save(organization);

        return organization;
    }

    @PostMapping("/delete/{orgId}")
    public void deleteRole(@PathVariable @NotNull String orgId) {
        ehOrganizationService.deleteById(orgId);
    }



}
