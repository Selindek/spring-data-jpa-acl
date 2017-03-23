package com.berrycloud.acl.sample.all.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.berrycloud.acl.repository.AclJpaRepository;
import com.berrycloud.acl.repository.NoAcl;
import com.berrycloud.acl.sample.all.entity.PersonHasPersonPermission;

@RepositoryRestResource(exported = false)
@NoAcl
public interface PersonHasPersonRepository extends AclJpaRepository<PersonHasPersonPermission, Long>{
}
