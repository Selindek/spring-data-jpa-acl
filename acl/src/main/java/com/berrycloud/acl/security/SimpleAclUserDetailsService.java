package com.berrycloud.acl.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.AclLogic;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class SimpleAclUserDetailsService implements AclUserDetailsService<SimpleGrantedAuthority> {
  // TODO extend it to a UserDetailsManager ???  
  // TODO make it work with other derived GrantedAuthorities ???

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
  protected AclUserDetails createUserDetails(AclUser<Serializable, AclRole<Serializable>> aclUser, Collection<? extends GrantedAuthority> authorities) {
    return new SimpleAclUserDetails(aclUser.getId(),aclUser.getUsername(), aclUser.getPassword(), authorities);
  }

  private Collection<? extends GrantedAuthority> createAuthorities(Set<AclRole<Serializable>> roleSet) {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    for (AclRole<Serializable> role : roleSet) {
      grantedAuthorities.add( createGrantedAuthority(role.getRoleName()));
    }
    // TODO load roles from groups
    return grantedAuthorities;
  }

  
  /**
   * Create a GrantedAuthority object from an authority String.
   * All acl methods use this method for creating authorities, so by overriding this method in a derived AclUserDetailsService one can 
   * introduce an extended GrantedAuthority object for the whole Acl/security/authentication framework
   */
  @Override
  public SimpleGrantedAuthority createGrantedAuthority(String authority) {
    return new SimpleGrantedAuthority(authority);
  }

  // Security utility methods
  // TODO move to separate bean ?

  @Override
  public AclUserDetails getCurrentUser() {
    return (AclUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  @Override
  public boolean isAdmin() {
    // TODO create constants for common authorities
    return hasAuthority("ROLE_ADMIN");
  }

  @Override
  public boolean hasAuthority(String authority) {
    return getCurrentUser().getAuthorities().contains(createGrantedAuthority(authority));
  }
  
 
}
