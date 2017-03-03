package com.berrycloud.acl.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AclUserDetailsService<A extends GrantedAuthority> extends UserDetailsService {

	@Override
	AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

	A createGrantedAuthority(String authority);

	AclUserDetails getCurrentUser();

	boolean isAdmin();

	boolean hasAuthority(String authority);

}
