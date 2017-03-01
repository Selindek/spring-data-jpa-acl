package com.berrycloud.acl.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AclUser<ID extends Serializable, R extends AclRole<? extends Serializable>> implements AclEntity<ID> {
  
  public abstract Set<R> getAclRoles();
  
  @Column(nullable=false, unique=true)
  public abstract String getUsername();

  public abstract String getPassword();
}
