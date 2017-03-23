package com.berrycloud.acl.sample.all.entity;

import javax.persistence.Entity;

import com.berrycloud.acl.domain.PermissionLink;


@Entity
public class PersonHasPersonPermission
	extends PermissionLink<Person, Person> {

    public PersonHasPersonPermission() {
    }

    public PersonHasPersonPermission(Person owner, Person target, String permission) {
	super(owner,target,permission);
    }

}
