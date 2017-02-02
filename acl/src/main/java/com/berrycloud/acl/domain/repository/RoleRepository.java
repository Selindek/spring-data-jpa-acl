package com.berrycloud.acl.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.berrycloud.acl.domain.SimpleAclRole;


@RepositoryRestResource(exported=false,collectionResourceRel = "roles", path = "roles")
public interface RoleRepository extends CrudRepository<SimpleAclRole, Integer> {

}
