package com.berrycloud.acl.sample.all.repository;

import com.berrycloud.acl.domain.SimpleAclRole;
import com.berrycloud.acl.repository.AclJpaRepository;

public interface RoleRepository extends AclJpaRepository<SimpleAclRole, Long>{
    
}
