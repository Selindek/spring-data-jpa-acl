package com.berrycloud.acl.sample.all.repository;

import com.berrycloud.acl.repository.AclJpaRepository;
import com.berrycloud.acl.sample.all.entity.TestGroup;

//@RepositoryRestResource(collectionResourceRel = "groups", path = "groups"/*, excerptProjection=EmptyProjection.class*/)
public interface GroupRepository extends AclJpaRepository<TestGroup, Integer>{
    
}
