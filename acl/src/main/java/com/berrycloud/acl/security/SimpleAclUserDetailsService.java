package com.berrycloud.acl.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SimpleAclUserDetailsService extends AbstractAclUserDetailsService<SimpleGrantedAuthority> {

	@Override
	public SimpleGrantedAuthority createGrantedAuthority(String authority) {
		return new SimpleGrantedAuthority(authority);
	}
}
