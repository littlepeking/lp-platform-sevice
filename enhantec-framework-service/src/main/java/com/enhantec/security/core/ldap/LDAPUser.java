package com.enhantec.security.core.ldap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

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
