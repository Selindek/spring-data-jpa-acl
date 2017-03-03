package com.berrycloud.acl.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.AclLogic;
import com.berrycloud.acl.AclUtils;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public abstract class AbstractAclUserDetailsService<A extends GrantedAuthority> implements AclUserDetailsService<A> {
	// TODO extend it to a UserDetailsManager ???

	@Autowired
	private AclLogic aclLogic;

	@Override
	@Transactional(readOnly = true)
	public AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		AclUser<Serializable, AclRole<Serializable>> aclUser = aclLogic.loadUserByUsername(username);

		return createUserDetails(aclUser, createAuthorities(aclLogic.getAllRoles(aclUser)));
	}

	/**
	 * Subclasses should override this method for creating extended AclUserDetails objects.
	 */
	protected AclUserDetails createUserDetails(AclUser<Serializable, AclRole<Serializable>> aclUser, Collection<A> authorities) {
		return new SimpleAclUserDetails(aclLogic.getUserId(aclUser), aclUser.getUsername(), aclUser.getPassword(), authorities);
	}

	protected Collection<A> createAuthorities(Set<AclRole<Serializable>> roleSet) {
		Set<A> grantedAuthorities = new HashSet<A>();
		for (AclRole<Serializable> role : roleSet) {
			grantedAuthorities.add(createGrantedAuthority(role.getRoleName()));
		}
		return grantedAuthorities;
	}

	/**
	 * Create a GrantedAuthority object from an authority String. All acl methods use this method for creating authorities, so by overriding
	 * this method in a derived AclUserDetailsService one can introduce an extended GrantedAuthority object for the whole
	 * Acl/security/authentication framework
	 */
	@Override
	public abstract A createGrantedAuthority(String authority); 

	// Security utility methods
	// TODO move to separate bean ?

	@Override
	public AclUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			return (AclUserDetails) authentication.getPrincipal();
		}
		return null;
	}

	@Override
	public boolean isAdmin() {
		return hasAuthority(AclUtils.ROLE_ADMIN);
	}

	@Override
	public boolean hasAuthority(String authority) {
		AclUserDetails currentUser = getCurrentUser();
		return currentUser != null && currentUser.getAuthorities().contains(createGrantedAuthority(authority));
	}

}
