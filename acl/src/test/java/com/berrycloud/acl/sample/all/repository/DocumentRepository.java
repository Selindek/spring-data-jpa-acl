package com.berrycloud.acl.sample.all.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.berrycloud.acl.repository.AclJpaRepository;
import com.berrycloud.acl.sample.all.entity.Document;
import com.berrycloud.acl.sample.all.entity.Person;

//@RepositoryRestResource(collectionResourceRel = "documents", path = "documents"/*, excerptProjection=EmptyProjection.class*/)
public interface DocumentRepository extends AclJpaRepository<Document, Integer>{
    
    //@NoAcl
    Page<Document> findByCreatorId(@Param("id") Integer id, Pageable page);

    List<Document> findByCreator(@Param("person") Person person);

    Person findCreatorById(@Param("id") Integer id);
}
