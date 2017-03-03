package com.berrycloud.acl.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AclUserDetailsService<A extends GrantedAuthority> extends UserDetailsService {

	@Override
	AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

	A createGrantedAuthority(String authority);

	AclUserDetails getCurrentUser();

	/**
	 * Checks if the current user is an administrator
	 * (Has ROLE_ADMIN role)
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

}
