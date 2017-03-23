package com.berrycloud.acl.sample.all.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.berrycloud.acl.sample.all.entity.Person;

@Service
public class PersonService {

	@PreAuthorize("hasPermission(#id, 'com.berrycloud.acl.sample.all.entity.Person', #permission)")
	public boolean loadPerson(Integer id, String permission) {
		return true;
	}

	@PreAuthorize("hasPermission(#person, #permission)")
	public boolean loadPerson(Person person, String permission) {
		return true;
	}

	@PreAuthorize("hasPermission(#id, 'com.berrycloud.acl.sample.all.entity.Invalid', #permission)")
	public boolean loadPersonInvalidType(Integer id, String permission) {
		return true;
	}

	@PreAuthorize("hasPermission(#person, #permission)")
	public boolean loadInvalidObject(String person, String permission) {
		return true;
	}
	
}
