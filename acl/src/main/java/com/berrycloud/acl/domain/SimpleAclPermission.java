package com.berrycloud.acl.domain;

import javax.persistence.Entity;

@Entity
public class SimpleAclPermission extends AclPermission<Integer>{

  public SimpleAclPermission() {
  }

  public SimpleAclPermission(final String permissionName) {
    super(permissionName);
  }
}
