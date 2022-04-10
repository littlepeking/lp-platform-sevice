package com.enhantec.security.ldap;

import com.enhantec.security.common.models.EHAuthority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.security.core.userdetails.UserDetails;

import javax.naming.Name;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entry(objectClasses = {"inetOrgPerson","organizationalPerson","person","top"})
@Data
public class LDAPUser {

    @Id
    @JsonIgnore
    private Name id;

    @Attribute(name = "uid")
    private String username;

    @Attribute(name = "userPassword")
    @JsonIgnore
    private String password;


}
