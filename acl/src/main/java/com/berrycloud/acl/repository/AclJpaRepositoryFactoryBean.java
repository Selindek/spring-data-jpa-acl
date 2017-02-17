package com.berrycloud.acl.repository;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.domain.AclEntity;

public class AclJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> {

  @Resource
  AclUserPermissionSpecification aclSpecification;

  public AclJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
    return new Factory(entityManager);
  }

  private class Factory extends JpaRepositoryFactory {

    public Factory(EntityManager entityManager) {
      super(entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
      return AclEntity.class.isAssignableFrom(metadata.getDomainType()) ? AclJpaRepository.class : super.getRepositoryBaseClass(metadata);
    }

    @Override
    protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
      SimpleJpaRepository<?, ?> repository = super.getTargetRepository(information, entityManager);
      if (repository instanceof AclJpaRepository) {
        ((AclJpaRepository<?, ?>) repository).setAclSpecification(aclSpecification);
      }
      return repository;
    }
  }

}
