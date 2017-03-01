package com.berrycloud.acl.repository;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentProperty;

public interface PropertyRepository<T, ID extends Serializable> {

	Object findProperty(ID id, PersistentProperty<? extends PersistentProperty<?>> property, Pageable pageable);

	Object findProperty(ID id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId);

	/**
	 * Helper method for calling clear on the EntityManager.
	 * 
	 */
	void clear();

}
