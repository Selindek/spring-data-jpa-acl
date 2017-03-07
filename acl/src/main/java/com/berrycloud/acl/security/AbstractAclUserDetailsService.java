package com.berrycloud.acl.security;

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

        AclUser<AclRole> aclUser = aclLogic.loadUserByUsername(username);

        return createUserDetails(aclUser, createAuthorities(aclLogic.getAllRoles(aclUser)));
    }

    /**
     * Subclasses should override this method for creating extended AclUserDetails objects.
     */
    protected AclUserDetails createUserDetails(AclUser<AclRole> aclUser, Collection<A> authorities) {
        return new SimpleAclUserDetails(aclLogic.getUserId(aclUser), aclUser.getUsername(), aclUser.getPassword(),
                authorities);
    }

    protected Collection<A> createAuthorities(Set<AclRole> roleSet) {
        Set<A> grantedAuthorities = new HashSet<A>();
        for (AclRole role : roleSet) {
            grantedAuthorities.add(createGrantedAuthority(role.getRoleName()));
        }
        return grantedAuthorities;
    }

    @Override
    public abstract A createGrantedAuthority(String authority);

    // Security utility methods

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

    @Override
    public boolean hasAnyAuthorities(Set<GrantedAuthority> authorities) {
        if (authorities.isEmpty()) {
            return true;
        }
        AclUserDetails currentUser = getCurrentUser();
        if (currentUser != null) {

            for (GrantedAuthority authority : authorities) {
                if (currentUser.getAuthorities().contains(authority)) {
                    return true;
                }
            }
        }
        return false;
    }
}
