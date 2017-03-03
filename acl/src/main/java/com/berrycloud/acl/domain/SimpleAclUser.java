package com.berrycloud.acl.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class SimpleAclUser implements AclUser<Integer, SimpleAclRole> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(unique = true, nullable = false)
	private String username;
	private String password;

	@ManyToMany
	private Set<SimpleAclRole> aclRoles;

	public SimpleAclUser() {
	}

	public SimpleAclUser(String username) {
		this(username, "password");
	}

	public SimpleAclUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	// @Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Set<SimpleAclRole> getAclRoles() {
		return aclRoles;
	}

	public void setAclRoles(Set<SimpleAclRole> aclRoles) {
		this.aclRoles = aclRoles;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
