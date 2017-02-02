package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Embeddable
@Entity
public abstract class AclPermission<ID extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private ID id;

    @Column(unique = true)
    private String permissionName;

    public AclPermission() {
    }

    public AclPermission(final String permissionName) {
	this.permissionName = permissionName;
    }

    public ID getId() {
	return id;
    }

    public void setId(ID id) {
	this.id = id;
    }

    public String getPermissionName() {
	return permissionName;
    }

    public void setPermissionName(final String permissionName) {
	this.permissionName = permissionName;
    }

    @Override
    public String toString() {
	return permissionName;
    }

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
	    return ((AclPermission<?>) object).getId() == null;
	}
	return getId().equals(((AclPermission<?>) object).getId());
    }
}
