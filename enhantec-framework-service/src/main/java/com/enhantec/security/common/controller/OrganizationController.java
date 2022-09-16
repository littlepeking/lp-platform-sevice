package com.enhantec.security.common.controller;

import com.enhantec.security.common.dto.OrganizationDTO;
import com.enhantec.security.common.model.EHOrganization;
import com.enhantec.security.common.service.EHOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author John Wang
 * @since 2022-04-18
 */
@RestController
@RequestMapping("/api/security/org")
@RequiredArgsConstructor
public class OrganizationController {

    private final EHOrganizationService ehOrganizationService;

    @GetMapping("/findAll")
    public List<EHOrganization> findAll() {
        return ehOrganizationService.list();
    }

    @GetMapping("/buildTree")
    public List<EHOrganization> buildTree(){
        return ehOrganizationService.buildOrgTree();
    }

    @GetMapping("/buildTreeByUserId")
    public List<EHOrganization> buildTreeByUserId(@RequestHeader String userId){
        return ehOrganizationService.buildOrgTreeByUserId(userId);
    }

    @GetMapping("/buildTreeByPermId")
    public List<EHOrganization> buildTreeByPermId(@RequestParam String permId){
        return ehOrganizationService.buildOrgTreeByPermId(permId);
    }

    @PostMapping("/createOrUpdate")
    public EHOrganization createOrUpdate(@Valid @RequestBody OrganizationDTO organizationDTO) {

        EHOrganization organization = EHOrganization.builder()
                .id(organizationDTO.getId())
                .parentId(organizationDTO.getParentId())
                .name(organizationDTO.getName())
                .code(organizationDTO.getCode())
                .address1(organizationDTO.getAddress1())
                .address2(organizationDTO.getAddress2())
                .contact1(organizationDTO.getContact1())
                .contact2(organizationDTO.getContact2())
                .version(organizationDTO.getVersion()).build();

        return ehOrganizationService.createOrUpdate(organization);

    }

    @DeleteMapping("/{orgId}")
    public void delete(@PathVariable @NotNull String orgId) {
        ehOrganizationService.deleteById(orgId);
    }


}
