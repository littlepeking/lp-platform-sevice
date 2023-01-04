/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec;

import com.enhantec.framework.security.core.ldap.LDAPUser;
import com.enhantec.framework.security.core.ldap.LdapUserRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@DataLdapTest
public class LDAPUserRepoTests {

    @Autowired
    LdapUserRepository ldapUserRepository;

    @Test
    public void givenUserNamePassword_Success(){
        String username = "admin";
        String password = "Passw0rd";
       val user = ldapUserRepository.findBysAMAccountNameAndPassword(username,password);
        List<LDAPUser> users = ldapUserRepository.findAll( );
       assertTrue(user.isPresent());
    }


    @Test
    public void givenUserNamePassword_Success1(){
        String username = "admin";
        String password = "Passw0rd";
        val user = ldapUserRepository.findAll();


    }


}
