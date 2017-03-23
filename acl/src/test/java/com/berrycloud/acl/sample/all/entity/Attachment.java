package com.berrycloud.acl.sample.all.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.annotation.AclRoleCondition;
import com.berrycloud.acl.annotation.AclRolePermission;

@Entity
@AclRolePermission(roles="ROLE_MANIPULATOR", value={"read","update"})
@AclRoleCondition(roles="ROLE_USER")
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String name;
	private String content;

	@AclParent("read")
	@AclOwner
	@ManyToOne(fetch = FetchType.LAZY)
	private Person creator;

	@ManyToOne
	@AclOwner
	@AclParent(prefix="attachments")
	private Document document;

	public Attachment() {
	}

	public Attachment(String name, String content, Person creator, Document document) {
		super();
		this.name = name;
		this.content = content;
		this.creator = creator;
		this.document = document;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Person getCreator() {
		return creator;
	}

	public void setCreator(Person creator) {
		this.creator = creator;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
