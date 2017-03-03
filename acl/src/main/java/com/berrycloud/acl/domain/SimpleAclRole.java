package com.berrycloud.acl.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.core.style.ToStringCreator;

@Entity
public class SimpleAclRole implements AclRole<Integer> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(unique = true, nullable = false)
	private String roleName;

	public SimpleAclRole() {
	}

	public SimpleAclRole(String roleName) {
		this.roleName = roleName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
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
