package com.berrycloud.acl.repository;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentProperty;

public interface PropertyRepository {

	Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Pageable pageable);

	Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId);

}
