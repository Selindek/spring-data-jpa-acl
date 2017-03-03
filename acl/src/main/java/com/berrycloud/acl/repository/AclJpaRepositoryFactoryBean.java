package com.berrycloud.acl.repository;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import com.berrycloud.acl.AclUserPermissionSpecification;

public class AclJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends JpaRepositoryFactoryBean<T, S, ID> {

	@Resource
	AclUserPermissionSpecification aclSpecification;

	public AclJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
		return new AclJpaRepositoryFactory(entityManager, aclSpecification);
	}

}
