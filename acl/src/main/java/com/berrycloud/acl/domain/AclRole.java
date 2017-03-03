package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.Column;

public interface AclRole<ID extends Serializable> extends AclEntity<ID> {

	@Column(nullable = false, unique = true)
	String getRoleName();

}
