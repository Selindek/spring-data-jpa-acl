package com.berrycloud.acl.security;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class SimpleAclUserDetails extends User implements AclUserDetails {

  private static final long serialVersionUID = 5998681433255152586L;

  private Serializable userId;

  public SimpleAclUserDetails(Serializable userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
    this(userId, username, password, true, true, true, true, authorities);
  }

  public SimpleAclUserDetails(Serializable userId, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
      boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    this.userId = userId;
  }

  @Override
  public Serializable getUserId() {
    return userId;
  }

}
