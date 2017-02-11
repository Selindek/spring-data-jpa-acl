package com.berrycloud.acl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.core.style.ToStringCreator;

@MappedSuperclass
public abstract class AclRole<ID extends Serializable> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private ID id;
    @Column(unique = true, nullable= false)
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
    
    @Override
    public String toString() { 
      return new ToStringCreator(this).append("roleName", roleName).toString();
    }
}
