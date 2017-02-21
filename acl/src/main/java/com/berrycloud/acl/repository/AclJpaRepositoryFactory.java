package com.berrycloud.acl.repository;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.domain.AclEntity;

public class AclJpaRepositoryFactory extends JpaRepositoryFactory{
  
  public AclUserPermissionSpecification aclSpecification;

  public AclJpaRepositoryFactory(EntityManager entityManager,AclUserPermissionSpecification aclSpecification) {
    super(entityManager);
    this.aclSpecification=aclSpecification;
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    return BaseAclJpaRepository.class;
  }

  protected boolean isAclRepository(RepositoryMetadata metadata) {
    NoAcl noAcl = metadata.getRepositoryInterface().getAnnotation(NoAcl.class);
    return AclEntity.class.isAssignableFrom(metadata.getDomainType()) && noAcl==null;
  }
  
  @Override
  protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
    SimpleJpaRepository<?, ?> repository = super.getTargetRepository(information, entityManager);
    ((BaseAclJpaRepository<?, ?>) repository).setAclSpecification(isAclRepository(information)?aclSpecification:null);
    return repository;
  }
  
  
//  @Override
//  protected QueryLookupStrategy getQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
//      return JpaQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider);
//  }
//  
}