package com.berrycloud.acl.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class SimpleAclUserDetails extends User implements AclUserDetails{

    private static final long serialVersionUID = 5998681433255152586L;

    private Serializable userId;

    public SimpleAclUserDetails(AclUser<Serializable,AclRole<Serializable>> aclUser) {
	super(aclUser.getUsername(),aclUser.getPassword(), true, true, true, true, createAuthorities(aclUser));
	userId = aclUser.getId();
    }
    
    private static Collection<? extends GrantedAuthority> createAuthorities(AclUser<Serializable,AclRole<Serializable>> aclUser) {
	Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
	     for (AclRole<Serializable> role : aclUser.getAclRoles()){
	            grantedAuthorities.add(new SimpleGrantedAuthority(role.getRoleName()));
	        }
	return grantedAuthorities;
    }

    @Override
    public Serializable getUserId() {
	return userId;
    }

}
