package com.berrycloud.acl.security;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AclUserDetailsService<A extends GrantedAuthority> extends UserDetailsService {

    @Override
    AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * Create a GrantedAuthority object from an authority String. All acl methods use this method for creating
     * authorities, so by overriding this method in a derived AclUserDetailsService one can introduce an extended
     * GrantedAuthority object for the whole Acl/security/authentication framework
     */
    A createGrantedAuthority(String authority);

    /**
     * Get the current user from the SecurityContext or null if there is no authentication
     *
     * @return
     */
    AclUserDetails getCurrentUser();

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

}
