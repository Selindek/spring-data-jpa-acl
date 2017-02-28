package com.berrycloud.acl.security.access;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import com.berrycloud.acl.repository.AclJpaRepository;

public class AclPermissionEvaluator implements PermissionEvaluator {

	private static Logger LOG = LoggerFactory.getLogger(AclPermissionEvaluator.class);

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private Repositories repositories;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		try {
			Class<?> domainClass = targetDomainObject.getClass();
			return hasPermission(authentication, getId(targetDomainObject), domainClass, permission);
		} catch (Exception ex) {
			LOG.debug("Invalid target for AclPermissionEvaluator: {}", targetDomainObject, ex);
			return false;
		}
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		try {
			return hasPermission(authentication, targetId, Class.forName(targetType), permission);
		} catch (ClassNotFoundException ex) {
			LOG.debug("Invalid target type AclPermissionEvaluator: {}", targetType, ex);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public <T, ID extends Serializable> boolean hasPermission(Authentication authentication, ID targetId, Class<T> domainClass, Object permission) {
		AclJpaRepository<T, ID> repository = (AclJpaRepository<T, ID>) lookUpAclJpaRepository(domainClass);
		if(repository==null) {
			LOG.debug("No AclJpaRepository was found for: {}", domainClass);
			return false;
		}
		String permissionString = getPermissionString(permission);
		T entity = repository.findOne(targetId, permissionString);
		return entity != null;
	}

	@SuppressWarnings("unchecked")
	private <T> Serializable getId(T object) {
		Class<T> domainClass = (Class<T>) object.getClass();
		JpaEntityInformation<T, ?> domainInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, em);
		Object id = domainInformation.getId(object);
		if (id instanceof Serializable) {
			return (Serializable) id;
		}
		throw new IllegalArgumentException("Id is not Serializable for " + domainClass);
	}

	protected String getPermissionString(Object permission) {
		return permission.toString();
	}

	@SuppressWarnings("unchecked")
	protected <T> AclJpaRepository<T, ?> lookUpAclJpaRepository(Class<T> domainClass) {
		Object repository = repositories.getRepositoryFor(domainClass);
		if (repository instanceof AclJpaRepository) {
			return (AclJpaRepository<T, ?>) repository;
		}

		return null;
	}
}
