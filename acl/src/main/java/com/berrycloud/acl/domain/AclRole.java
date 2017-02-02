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
public abstract class AclRole<ID extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private ID id;
    @Column(unique = true)
    private String roleName;

    public AclRole() {
    }

    public AclRole(String roleName) {
	this.roleName = roleName;
    }

    public ID getId() {
	return id;
    }

    public void setId(ID id) {
	this.id = id;
    }

    public String getRoleName() {
	return roleName;
    }

    public void setRoleName(String roleName) {
	this.roleName = roleName;
    }
}
