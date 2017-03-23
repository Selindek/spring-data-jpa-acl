package com.berrycloud.acl.sample.all.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.berrycloud.acl.repository.AclJpaRepository;
import com.berrycloud.acl.repository.NoAcl;
import com.berrycloud.acl.sample.all.entity.Person;


@NoAcl
public interface PersonRepositoryNoAcl extends AclJpaRepository<Person, Integer>, JpaSpecificationExecutor<Person>{

    Person findByUsername(@Param("username") String username);
    
    List<Person> findAllByOrderByUsernameDesc();


}
