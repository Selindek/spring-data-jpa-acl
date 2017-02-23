package com.berrycloud.acl.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AclUser<ID extends Serializable, R extends AclRole<? extends Serializable>> extends AclEntity<ID> {
  
  public abstract Set<R> getAclRoles();
  
  public abstract String getUsername();

  public abstract String getPassword();
}
