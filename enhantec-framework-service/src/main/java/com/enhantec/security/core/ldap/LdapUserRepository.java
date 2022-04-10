package com.enhantec.security.core.ldap;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LdapUserRepository extends LdapRepository<LDAPUser> {

    Optional<LDAPUser> findByUsernameAndPassword(String username, String password);
}
