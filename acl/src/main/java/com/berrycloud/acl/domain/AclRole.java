package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.springframework.core.style.ToStringCreator;

@MappedSuperclass
public abstract class AclRole<ID extends Serializable> extends AclEntity<ID>{

  public abstract String getRoleName();

  @Override
  public int hashCode() {
    return getId() == null ? 0 : getId().hashCode();
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null || !object.getClass().equals(this.getClass())) {
      return false;
    }
    if (getId() == null) {
      return ((AclRole<?>) object).getId() == null;
    }
    return getId().equals(((AclRole<?>) object).getId());
  }

  @Override
  public String toString() {
    return new ToStringCreator(this).append("roleName", getRoleName()).toString();
  }
}
