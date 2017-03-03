package com.berrycloud.acl.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;

public interface AclUser<ID extends Serializable, R extends AclRole<? extends Serializable>> extends AclEntity<ID> {

	Set<R> getAclRoles();

	@Column(nullable = false, unique = true)
	String getUsername();

	String getPassword();
}
