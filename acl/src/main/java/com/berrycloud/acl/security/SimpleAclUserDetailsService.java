package com.berrycloud.acl.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class SimpleAclUserDetailsService implements AclUserDetailsService<SimpleGrantedAuthority> {
  // TODO extend it to a UserDetailsManager ???  
  // TODO make it work with other derived GrantedAuthorities ???


  @PersistenceContext
  private EntityManager em;

  private Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType;

  @Autowired
  private void setAclUserType(AclMetaData aclMetaData) {
    aclUserType = aclMetaData.getAclUserType();
  }

  @Override
  @Transactional(readOnly = true)
  public AclUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AclUser<Serializable, AclRole<Serializable>>> query = cb.createQuery(aclUserType);
    Root<AclUser<Serializable, AclRole<Serializable>>> root = query.from(aclUserType);
    query.select(root).where(cb.equal(root.get("username"), username));

    AclUser<Serializable, AclRole<Serializable>> aclUser = em.createQuery(query).getSingleResult();
    if (aclUser == null) {
      throw (new UsernameNotFoundException("User with username'" + username + "' cannot be found."));
    }

    return new SimpleAclUserDetails(aclUser.getId(),aclUser.getUsername(), aclUser.getPassword(), createAuthorities(aclUser));
  }
  
  private Collection<? extends GrantedAuthority> createAuthorities(AclUser<Serializable, AclRole<Serializable>> aclUser) {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    for (AclRole<Serializable> role : aclUser.getAclRoles()) {
      grantedAuthorities.add( createGrantedAuthority(role.getRoleName()));
    }
    // TODO load roles from groups
    return grantedAuthorities;
  }

  @Override
  public SimpleGrantedAuthority createGrantedAuthority(String authority) {
    return new SimpleGrantedAuthority(authority);
  }


}
