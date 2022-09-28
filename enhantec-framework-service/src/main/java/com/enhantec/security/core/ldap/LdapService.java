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



package com.enhantec.security.core.ldap;

import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
@RequiredArgsConstructor
public class LdapService {
    private final LdapTemplate ldapTemplate;
    private Set<String> groups = new HashSet<>();

    public List<String> getAllGroupsForUser(String userCN){

        /*
         * Get user distinguised name, example: "user" -> "CN=User Name,OU=Groups,OU=Domain Users,DC=company,DC=something,DC=org"
         * This will be used for our query later
         */
        String distinguishedName = ldapTemplate.search(
                query().where("sAMAccountName").is(userCN),
                (AttributesMapper<String>) attrs -> attrs.get("distinguishedName").get().toString()
        ).get(0); //.get(0): we assume that search will return a result

        /*
         * This one recursively search for all (nested) group that this user belongs to
         * "member:1.2.840.113556.1.4.1941:" is a magic attribute, Reference:
         * https://msdn.microsoft.com/en-us/library/aa746475(v=vs.85).aspx
         * However, this filter is usually slow in case your ad directory is large.
         */
        List<String> allGroups = ldapTemplate.search(
                query().searchScope(SearchScope.SUBTREE)
                        .where("member:1.2.840.113556.1.4.1941:").is(distinguishedName),
                (AttributesMapper<String>) attrs -> attrs.get("cn").get().toString()
        );

        return allGroups;

    }

    /**
     * Retrieve all groups that this user belongs to.
     */
    public Set<String> getAllGroupsForUserRecursively(String userCN) {
        List<String> distinguishedNames = this.ldapTemplate.search(
                query().where("objectCategory").is("user").and(
                        query().where("sAMAccountName").is(userCN)
                                .or(query().where("userPrincipalName").is(userCN))
                ),
                (AttributesMapper<String>) attrs -> attrs.get("distinguishedName").get().toString()
        );

        if (distinguishedNames.isEmpty()) {
            throw new UsernameNotFoundException("User not recognized in LDAP");
        }

        return this.getAllGroupsRecursivelyByUserDistinguishedName(distinguishedNames.get(0), null);
    }

    private Set<String> getAllGroupsRecursivelyByUserDistinguishedName(String dn, @Nullable String parentDN) {
        List<String> results = this.ldapTemplate.search(
                query().where("member").is(dn),
                (AttributesMapper<String>) attrs -> attrs.get("distinguishedName").get().toString()
        );

        for (String result : results) {
            if (!(result.equals(parentDN) //circular, ignore
                    || this.groups.contains(result) //duplicate, ignore
            )) {
                this.getAllGroupsRecursivelyByUserDistinguishedName(result, dn);
            }
        }
        this.groups.addAll(results);

        return this.groups;
    }


}