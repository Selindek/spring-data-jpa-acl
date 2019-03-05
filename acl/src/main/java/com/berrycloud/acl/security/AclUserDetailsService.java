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
package com.berrycloud.acl.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.berrycloud.acl.AclConstants;
import com.berrycloud.acl.domain.AclRole;

/**
 * Extension of {@link UserDetailsService}. It creates an additional method {@link #createGrantedAuthority} what should
 * be used for creating a GrantedAuthorithy object from the role strings stored in the {@link AclRole} domain entities
 * 
 * @author István Rátkai (Selindek)
 *
 */
public interface AclUserDetailsService<A extends GrantedAuthority> extends UserDetailsService {

  /**
   * {@inheritDoc}
   */
  @Override
  AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

  /**
   * Create a GrantedAuthority object from an authority String. All acl methods use this method for creating
   * authorities, so by overriding this method in a derived AclUserDetailsService one can introduce an extended
   * GrantedAuthority object for the whole Acl/security/authentication framework
   */
  A createGrantedAuthority(String authority);

  /**
   * Get the Principal from the SecurityContext or null if there is no authentication
   */
  public static Object getPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      return authentication.getPrincipal();
    }
    return null;
  }

  /**
   * Get the current user's AclUserDetails object from the securityContext or null if it's not an AclUserDetails
   */
  public static AclUserDetails getAclUserDetails() {
    Object principal = getPrincipal();
    if (principal instanceof AclUserDetails) {
      return (AclUserDetails) principal;
    }
    return null;
  }

  /**
   * Get the current user's UserDetails object from the securityContext or null if it's not an UserDetails
   */
  public static UserDetails getUserDetails() {
    Object principal = getPrincipal();
    if (principal instanceof UserDetails) {
      return (UserDetails) principal;
    }
    return null;
  }

  /**
   * Get the current user's username from the securityContext or null if it cannot be found
   */
  public static String getUsername() {
    Object principal = getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    } else if (principal != null) {
      return principal.toString();
    }
    return null;
  }

  /**
   * Checks if the current user is an administrator (Has ROLE_ADMIN role)
   */
  public static boolean isAdmin() {
    return hasAuthority(AclConstants.ROLE_ADMIN);
  }

  /**
   * Checks if the current user has the given authority
   *
   * @param authority
   */
  public static boolean hasAuthority(String authority) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authority != null && authentication != null) {
      for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
        if (authority.equals(grantedAuthority.getAuthority())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if any authority of the current principal is in the provided set. If the set is empty it automatically
   * returns true. (Empty set means: ANY authority)
   *
   * @param authorities
   */
  public static boolean hasAnyAuthorities(String[] authorities) {
    if (authorities.length == 0) {
      return true;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null) {
      for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
        for (String authority : authorities) {
          if (authority.equals(grantedAuthority.getAuthority())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
