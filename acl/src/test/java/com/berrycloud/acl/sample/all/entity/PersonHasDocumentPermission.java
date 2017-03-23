package com.berrycloud.acl.sample.all.entity;

import javax.persistence.Entity;

import com.berrycloud.acl.domain.PermissionLink;


@Entity
public class PersonHasDocumentPermission
	extends PermissionLink<Person, Document> {

    public PersonHasDocumentPermission() {
    }

    public PersonHasDocumentPermission(Person owner, Document target, String permission) {
	super(owner,target,permission);
    }

}
