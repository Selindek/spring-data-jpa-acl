package com.berrycloud.acl.domain;

import javax.persistence.Entity;

@Entity
public class SimpleAclRole extends AclRole<Integer>{

    public SimpleAclRole() {
    }

    public SimpleAclRole(String roleName) {
	super(roleName);
    }
}
