package com.berrycloud.acl.repository;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AclJpaRepository<T, ID extends Serializable> extends PropertyRepository<T, ID>, JpaRepository<T, ID> {

	/**
	 * Find an entity by id with testing the current user's permission against the given permission during permission-check
	 * 
	 * @param id the id of the entity
	 * @param permission the permission we check against
	 * @return the entity with the given id or null if it's not exist or the current user has no proper permission to it
	 */
	T findOne(ID id, String permission);

	/**
	 * Find an entity by id with testing the current user's permission against the given permission during permission-check
	 * 
	 * @param id the id of the entity
	 * @param permission the permission we check against
	 * @return the entity with the given id
	 * @throws EntityNotFoundException if the entity cannot be found or the current user has no proper permission to it
	 */
	T getOne(ID id, String permission);

	/**
	 * Returns all instances of the type filtered by the given permission.
	 * 
	 * @return all entities the current user has the given permission to
	 */
	List<T> findAll(String permission);

	T findOne(Specification<T> spec, String permission);

}
