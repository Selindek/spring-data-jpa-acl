package com.berrycloud.acl.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AclUserDetailsService extends UserDetailsService{

    @Override
    AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
