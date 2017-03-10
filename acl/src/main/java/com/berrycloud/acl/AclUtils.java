package com.berrycloud.acl;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.berrycloud.acl.security.AclUserDetails;

public interface AclUtils {

    /**
     * Get the Principal from the SecurityContext or null if there is no authentication
     *
     * @return
     */
    Object getPrincipal();

    /**
     * Checks if the current user is an administrator (Has ROLE_ADMIN role)
     *
     * @return
     */
    boolean isAdmin();

    /**
     * Checks if the current user has the given authority
     *
     * @param authority
     * @return
     */
    boolean hasAuthority(String authority);

    /**
     * Checks if any authority of the current user is in the provided set. If the set is empty it automatically returns
     * true. (Empty set means: ANY authority)
     *
     * @param authorities
     * @return
     */
    boolean hasAnyAuthorities(Set<GrantedAuthority> authorities);

    /**
     * Get the current user's AclUserDetails object from the securityContext or null if it's not an AclUserDetails
     */
    AclUserDetails getAclUserDetails();

    /**
     * Get the current user's UserDetails object from the securityContext or null if it's not an UserDetails
     */
    UserDetails getUserDetails();

    /**
     * Get the current user's username from the securityContext or null if it cannot be found
     */
    String getUsername();
}
