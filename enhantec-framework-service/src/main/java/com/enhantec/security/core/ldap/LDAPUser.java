package com.enhantec.security.core.ldap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entry(objectClasses = {"user","organizationalPerson","person","top"})
@Data
public class LDAPUser {

    @Id
    @JsonIgnore
    private Name id;

    //姓名(显示在用户列表的名字，不唯一，因为相同名字可能存在不同的子目录下)
    @DnAttribute(value = "uid",index = 0)
    private String uid;
    //账号(用户登录使用的名称,保证全局唯一)
    @Attribute(name = "sAMAccountName")
    private String sAMAccountName;

    @Attribute(name = "cn")
    private String name;

    @Attribute(name = "userPassword")
    @JsonIgnore
    private String password;

    @JsonIgnore
    @Attribute(name = "distinguishedname")
    private Name dn;

    @Attribute(name = "department")
    private String deptName;

    @Attribute(name = "company")
    private String compName;

    @Attribute(name = "email")
    private String email;

    @Attribute(name = "mobile")
    private String mobile;





}
