package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.core.style.ToStringCreator;

@MappedSuperclass
public abstract class AclRole<ID extends Serializable> implements AclEntity<ID> {

	@Column(nullable = false, unique = true)
	public abstract String getRoleName();

	@Override
	public int hashCode() {
		return getRoleName() == null ? 0 : getRoleName().hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null || !object.getClass().equals(this.getClass())) {
			return false;
		}
		if (getRoleName() == null) {
			return ((AclRole<?>) object).getRoleName() == null;
		}
		return getRoleName().equals(((AclRole<?>) object).getRoleName());
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("roleName", getRoleName()).toString();
	}
}
