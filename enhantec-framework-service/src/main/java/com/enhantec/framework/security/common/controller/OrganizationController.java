/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.framework.security.common.controller;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.security.common.model.EHOrganization;
import com.enhantec.framework.security.common.service.EHOrganizationService;
import com.enhantec.framework.security.common.dto.OrganizationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/security/org")
@RequiredArgsConstructor
public class OrganizationController {

    private final EHOrganizationService ehOrganizationService;

    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @GetMapping("/findAll")
    public List<EHOrganization> findAll() {
        return ehOrganizationService.list();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/listByIds")
    public List<EHOrganization> listByIds(@RequestBody List<String> ids) {
        return ehOrganizationService.listByIds(ids);
    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @GetMapping("/buildTree")
    public List<EHOrganization> buildTree(){
        return ehOrganizationService.buildOrgTree();
    }


    @PreAuthorize("hasAuthority('SCHEDULER_SCHEDULE')")
    @GetMapping("/buildSubOrgTreeByOrgId")
    public EHOrganization buildSubOrgTreeByOrgId(@RequestParam String orgId){
        //TODO:CHECK ORG PARAM PERM for current user
        return ehOrganizationService.buildSubOrgTreeByOrgId(orgId);
    }

    //@PreAuthorize("permitAll()")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/buildTreeByUserId")
    public List<EHOrganization> buildTreeByUserId(){
        return ehOrganizationService.buildOrgTreeByUserId(EHContextHelper.getUser().getId());
    }
    @PreAuthorize("hasAnyAuthority('SECURITY_PERMISSION')")
    @GetMapping("/buildTreeByPermId")
    public List<EHOrganization> buildTreeByPermId(@RequestParam String permId){
        return ehOrganizationService.buildOrgTreeByPermId(permId);
    }


    //@PreAuthorize("hasAnyAuthority('SECURITY_ORG','SECURITY_USER')")
    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @PostMapping("/createOrUpdate")
    public EHOrganization createOrUpdate(@Valid @RequestBody OrganizationDTO organizationDTO) {

        EHOrganization organization = EHOrganization.builder()
                .id(organizationDTO.getId())
                .parentId(organizationDTO.getParentId())
                .name(organizationDTO.getName())
                .code(organizationDTO.getCode())
                .connectionStringParams(organizationDTO.getConnectionStringParams())
                .address1(organizationDTO.getAddress1())
                .address2(organizationDTO.getAddress2())
                .contact1(organizationDTO.getContact1())
                .contact2(organizationDTO.getContact2())
                .version(organizationDTO.getVersion()).build();

        return ehOrganizationService.createOrUpdate(organization);

    }

    @PreAuthorize("hasAnyAuthority('SECURITY_ORG')")
    @DeleteMapping("/{orgId}")
    public void delete(@PathVariable @NotNull String orgId) {
        ehOrganizationService.deleteById(orgId);
    }


}
