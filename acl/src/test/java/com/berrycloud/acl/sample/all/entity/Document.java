package com.berrycloud.acl.sample.all.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.berrycloud.acl.annotation.AclOwner;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
// @AclRolePermission(role = {})
// @AclRolePermission(role = { "ROLE_ADMIN" })
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String name;
	private String content;

	// @AclParent("read")
	@AclOwner
	@ManyToOne(fetch = FetchType.LAZY)
	private Person creator;

	@OneToMany(mappedBy = "target")
	private List<PersonHasDocumentPermission> personOwner = new ArrayList<>();

	@OneToMany(mappedBy = "document")
	private List<Attachment> attachments = new ArrayList<>();


	public Document() {
	}

	public Document(String name, String content, Person creator) {
		super();
		this.name = name;
		this.content = content;
		this.creator = creator;
	}

	// @Override
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

	@JsonIgnore
	public List<PersonHasDocumentPermission> getPersonOwner() {
		return personOwner;
	}

	public void setPersonOwner(List<PersonHasDocumentPermission> personOwner) {
		this.personOwner = personOwner;
	}
	

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

}
