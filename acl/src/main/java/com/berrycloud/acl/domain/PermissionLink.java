package com.berrycloud.acl.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.berrycloud.acl.annotation.AclOwner;

@MappedSuperclass
public abstract class PermissionLink<O extends AclEntity<?>, T extends AclEntity<?>> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@AclOwner
	@ManyToOne()
	@JoinColumn(nullable = false, updatable = false)
	private O owner;

	@ManyToOne()
	@JoinColumn(nullable = false, updatable = false)
	private T target;

	@Column(nullable = false, updatable = false)
	private String permission;

	public PermissionLink() {
	}

	public PermissionLink(Integer id) {
		this.id = id;
	}

	public PermissionLink(O owner, T target, String permission) {
		this.owner = owner;
		this.target = target;
		this.permission = permission;
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public O getOwner() {
		return owner;
	}

	public void setOwner(final O owner) {
		this.owner = owner;
	}

	public T getTarget() {
		return target;
	}

	public void setTarget(final T target) {
		this.target = target;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(final String permission) {
		this.permission = permission;
	}

}
