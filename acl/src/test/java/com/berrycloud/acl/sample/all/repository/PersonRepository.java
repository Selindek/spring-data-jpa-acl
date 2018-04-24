package com.berrycloud.acl.sample.all.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.berrycloud.acl.repository.AclJpaRepository;
import com.berrycloud.acl.sample.all.entity.Person;

//@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface PersonRepository extends AclJpaRepository<Person, Integer>, JpaSpecificationExecutor<Person> {

    // @Modifying
    // @NoAcl
    @Query("select p from Person p")
    List<Person> selectAllUsingNative();

    List<Person> findByLastName(@Param("name") String name);

    Person findByUsername(@Param("username") String username);

    List<Person> findAllByOrderByUsernameDesc();

    List<Person> findAllByOrderByLastNameDesc();

    List<Person> findAllByOrderByIdAsc();

    Long countByIdGreaterThan(@Param("id") Integer id);
}
