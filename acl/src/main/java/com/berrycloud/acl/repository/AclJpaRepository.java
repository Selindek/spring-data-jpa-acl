package com.berrycloud.acl.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AclJpaRepository<T, ID extends Serializable> extends PropertyRepository, JpaRepository<T, ID>{
	
}
