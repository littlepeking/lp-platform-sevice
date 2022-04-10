package com.enhantec.test.security.ldap;

import com.enhantec.security.ldap.LdapUserRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
       val user = ldapUserRepository.findByUsernameAndPassword(username,password);

       assertTrue(user.isPresent());
    }


    @Test
    public void givenUserNamePassword_Success1(){
        String username = "admin";
        String password = "Passw0rd";
        val user = ldapUserRepository.findAll();

       
    }


}
