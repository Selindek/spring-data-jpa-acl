package com.berrycloud.acl.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

}
