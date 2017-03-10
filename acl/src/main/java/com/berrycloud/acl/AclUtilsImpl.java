/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.berrycloud.acl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;

/**
 * Utility methods for ACL Security.
 *
 * @author István Rátkai (Selindek)
 */
public class AclUtilsImpl implements AclUtils {

    @Autowired
    AclUserDetailsService<?> aclUserDetailsService;

    @Override
    public Object getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public AclUserDetails getAclUserDetails() {
        Object principal = getPrincipal();
        if (principal instanceof AclUserDetails) {
            return (AclUserDetails) principal;
        }
        return null;
    }

    @Override
    public UserDetails getUserDetails() {
        Object principal = getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    @Override
    public String getUsername() {
        Object principal = getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal != null) {
            return principal.toString();
        }
        return null;
    }

    @Override
    public boolean isAdmin() {
        return hasAuthority(AclConstants.ROLE_ADMIN);
    }

    @Override
    public boolean hasAuthority(String authority) {
        UserDetails currentUser = getUserDetails();
        return currentUser != null
                && currentUser.getAuthorities().contains(aclUserDetailsService.createGrantedAuthority(authority));
    }

    @Override
    public boolean hasAnyAuthorities(Set<GrantedAuthority> authorities) {
        if (authorities.isEmpty()) {
            return true;
        }
        UserDetails currentUser = getUserDetails();
        if (currentUser != null) {

            for (GrantedAuthority authority : authorities) {
                if (currentUser.getAuthorities().contains(authority)) {
                    return true;
                }
            }
        }
        return false;
    }

}