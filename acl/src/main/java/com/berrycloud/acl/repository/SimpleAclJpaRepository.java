/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.berrycloud.acl.repository;

import static com.berrycloud.acl.AclUtils.DELETE_PERMISSION;
import static com.berrycloud.acl.AclUtils.READ_PERMISSION;
import static com.berrycloud.acl.AclUtils.UPDATE_PERMISSION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.repository.support.PageableExecutionUtils.TotalSupplier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.berrycloud.acl.AclUserPermissionSpecification;

/**
 * Default implementation of the {@link AclJpaRepository} interface. This class uses the default SimpleJpaRepository
 * methods and logic and extends it with the ACL support
 * 
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author István Rátkai
 * 
 * @param <T> the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 */
@Repository
@Transactional(readOnly = true)
public class SimpleAclJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements PropertyRepository, JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

	private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

	private final JpaEntityInformation<T, ?> entityInformation;
	private final EntityManager em;

	private CrudMethodMetadata metadata;
	private AclUserPermissionSpecification aclSpecification;

	/**
	 * Creates a new {@link SimpleAclJpaRepository} to manage objects of the given {@link JpaEntityInformation}.
	 * 
	 * @param entityInformation must not be {@literal null}.
	 * @param entityManager must not be {@literal null}.
	 */
	public SimpleAclJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);

		this.entityInformation = entityInformation;
		this.em = entityManager;
	}

	/**
	 * Creates a new {@link SimpleJpaRepository} to manage objects of the given domain type.
	 * 
	 * @param domainClass must not be {@literal null}.
	 * @param em must not be {@literal null}.
	 */
	public SimpleAclJpaRepository(Class<T> domainClass, EntityManager em) {
		this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
	}

	/**
	 * Configures a custom {@link CrudMethodMetadata} to be used to detect {@link LockModeType}s and query hints to be
	 * applied to queries.
	 * 
	 * @param crudMethodMetadata
	 */
	@Override
	public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
		this.metadata = crudMethodMetadata;
	}

	public void setAclSpecification(AclUserPermissionSpecification aclSpecification) {
		this.aclSpecification = aclSpecification;
	}

	@Override
	protected CrudMethodMetadata getRepositoryMethodMetadata() {
		return metadata;
	}

	@Override
	protected Class<T> getDomainClass() {
		return entityInformation.getJavaType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#delete(java.io. Serializable)
	 */
	@Override
	@Transactional
	public void delete(ID id) {
		T entity = findOne(id, DELETE_PERMISSION);

		if (entity == null) {
			throw new EmptyResultDataAccessException(String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);
		}
		doDelete(entity);

	}

	private void doDelete(T entity) {
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#delete(java.lang. Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void delete(T entity) {

		Assert.notNull(entity, "The entity must not be null!");
		delete((ID) (entityInformation.getId(entity)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#delete(java.lang. Iterable)
	 */
	@Override
	@Transactional
	public void delete(Iterable<? extends T> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		for (T entity : entities) {
			delete(entity);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#deleteInBatch(java. lang.Iterable)
	 */
	@Override
	@Transactional
	public void deleteInBatch(Iterable<T> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");

		Iterator<T> iterator = entities.iterator();
		if (!iterator.hasNext()) {
			return;
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(getDomainClass());
		Root<T> root = delete.from(getDomainClass());

		ArrayList<T> list = new ArrayList<>();
		for (T e : entities) {
			list.add(e);
		}
		Predicate inPredicate = root.in(list);

		if (aclSpecification != null) {
			inPredicate = cb.and(inPredicate, aclSpecification.toPredicate(root, delete, cb, DELETE_PERMISSION));
		}
		delete.where(inPredicate);

		em.createQuery(delete).executeUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.Repository#deleteAll()
	 */
	@Override
	@Transactional
	public void deleteAll() {

		for (T element : findAll(DELETE_PERMISSION)) {
			doDelete(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#deleteAllInBatch()
	 */
	@Override
	@Transactional
	public void deleteAllInBatch() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(getDomainClass());
		Root<T> root = delete.from(getDomainClass());

		if (aclSpecification != null) {
			delete.where(aclSpecification.toPredicate(root, delete, cb, DELETE_PERMISSION));
		}

		em.createQuery(delete).executeUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#findOne(java.io. Serializable)
	 */
	@Override
	public T findOne(ID id) {
		return findOne(id, READ_PERMISSION);
	}

	public T findOne(ID id, String permission) {
		Assert.notNull(id, ID_MUST_NOT_BE_NULL);
		return findOne(new Specification<T>() {

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.equal(root.get(entityInformation.getIdAttribute()), id);
			}

		}, permission);

	}

	/**
	 * Returns a {@link Map} with the query hints based on the current {@link CrudMethodMetadata} and potential
	 * {@link EntityGraph} information.
	 * 
	 * @return
	 */
	@Override
	protected Map<String, Object> getQueryHints() {

		if (metadata.getEntityGraph() == null) {
			return metadata.getQueryHints();
		}

		Map<String, Object> hints = new HashMap<String, Object>();
		hints.putAll(metadata.getQueryHints());

		hints.putAll(Jpa21Utils.tryGetFetchGraphHints(em, getEntityGraph(), getDomainClass()));

		return hints;
	}

	private JpaEntityGraph getEntityGraph() {

		String fallbackName = this.entityInformation.getEntityName() + "." + metadata.getMethod().getName();
		return new JpaEntityGraph(metadata.getEntityGraph(), fallbackName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#getOne(java.io. Serializable)
	 */
	@Override
	public T getOne(ID id) {
		return getOne(id, READ_PERMISSION);
	}

	public T getOne(ID id, String permission) {

		T entity = this.findOne(id, permission);
		if (entity == null) {
			throw new EntityNotFoundException("Unable to find " + getDomainClass().getName() + " with id " + id);
		}
		return entity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#exists(java.io. Serializable)
	 */
	@Override
	public boolean exists(ID id) {
		return findOne(id) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#findAll()
	 */
	@Override
	public List<T> findAll() {
		return findAll(READ_PERMISSION);
	}

	public List<T> findAll(String permission) {
		return getQuery(null, (Sort) null, permission).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#findAll(ID[])
	 */
	@Override
	public List<T> findAll(Iterable<ID> ids) {

		if (ids == null || !ids.iterator().hasNext()) {
			return Collections.emptyList();
		}

		if (entityInformation.hasCompositeId()) {

			List<T> results = new ArrayList<T>();

			for (ID id : ids) {
				results.add(findOne(id));
			}

			return results;
		}

		ByIdsSpecification<T> specification = new ByIdsSpecification<T>(entityInformation);
		TypedQuery<T> query = getQuery(specification, (Sort) null);

		return query.setParameter(specification.parameter, ids).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#findAll(org. springframework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Sort sort) {
		return getQuery(null, sort).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(
	 * org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Pageable pageable) {

		if (null == pageable) {
			return new PageImpl<T>(findAll());
		}

		return findAll((Specification<T>) null, pageable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findOne(
	 * org.springframework.data.jpa.domain.Specification)
	 */
	@Override
	public T findOne(Specification<T> spec) {
		return findOne(spec, READ_PERMISSION);
	}

	public T findOne(Specification<T> spec, String permission) {

		try {
			return getQuery(spec, (Sort) null, permission).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(
	 * org.springframework.data.jpa.domain.Specification)
	 */
	@Override
	public List<T> findAll(Specification<T> spec) {
		return getQuery(spec, (Sort) null).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(
	 * org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Specification<T> spec, Pageable pageable) {

		TypedQuery<T> query = getQuery(spec, pageable);
		return pageable == null ? new PageImpl<T>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#findAll(
	 * org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Specification<T> spec, Sort sort) {
		return getQuery(spec, sort).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findOne(
	 * org.springframework.data.domain.Example)
	 */
	@Override
	public <S extends T> S findOne(Example<S> example) {
		try {
			return getQuery(new ExampleSpecification<S>(example), example.getProbeType(), (Sort) null).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#count(
	 * org.springframework.data.domain.Example)
	 */
	@Override
	public <S extends T> long count(Example<S> example) {
		return executeCountQuery(getCountQuery(new ExampleSpecification<S>(example), example.getProbeType(), READ_PERMISSION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#exists(
	 * org.springframework.data.domain.Example)
	 */
	@Override
	public <S extends T> boolean exists(Example<S> example) {
		return !getQuery(new ExampleSpecification<S>(example), example.getProbeType(), (Sort) null).getResultList().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findAll(
	 * org.springframework.data.domain.Example)
	 */
	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		return getQuery(new ExampleSpecification<S>(example), example.getProbeType(), (Sort) null).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findAll(
	 * org.springframework.data.domain.Example, org.springframework.data.domain.Sort)
	 */
	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		return getQuery(new ExampleSpecification<S>(example), example.getProbeType(), sort).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.query.QueryByExampleExecutor#findAll(
	 * org.springframework.data.domain.Example, org.springframework.data.domain.Pageable)
	 */
	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {

		ExampleSpecification<S> spec = new ExampleSpecification<S>(example);
		Class<S> probeType = example.getProbeType();
		TypedQuery<S> query = getQuery(new ExampleSpecification<S>(example), probeType, pageable);

		return pageable == null ? new PageImpl<S>(query.getResultList()) : readPage(query, probeType, pageable, spec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#count()
	 */
	@Override
	public long count() {
		return count((Specification<T>) null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaSpecificationExecutor#count(
	 * org.springframework.data.jpa.domain.Specification)
	 */
	@Override
	public long count(Specification<T> spec) {
		return executeCountQuery(getCountQuery(spec, getDomainClass(), READ_PERMISSION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public <S extends T> S save(S entity) {

		if (entityInformation.isNew(entity)) {
			em.persist(entity);
			return entity;
		} else {
			getOne((ID) (entityInformation.getId(entity)), UPDATE_PERMISSION);
			return em.merge(entity);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#saveAndFlush(java. lang.Object)
	 */
	@Override
	@Transactional
	public <S extends T> S saveAndFlush(S entity) {

		S result = save(entity);
		flush();

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#save(java.lang. Iterable)
	 */
	@Override
	@Transactional
	public <S extends T> List<S> save(Iterable<S> entities) {

		List<S> result = new ArrayList<S>();

		if (entities == null) {
			return result;
		}

		for (S entity : entities) {
			result.add(save(entity));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.JpaRepository#flush()
	 */
	@Override
	@Transactional
	public void flush() {

		em.flush();
	}

	/**
	 * Reads the given {@link TypedQuery} into a {@link Page} applying the given {@link Pageable} and
	 * {@link Specification}.
	 *
	 * @param query must not be {@literal null}.
	 * @param spec can be {@literal null}.
	 * @param pageable can be {@literal null}.
	 * @return
	 * @deprecated use {@link #readPage(TypedQuery, Class, Pageable, Specification)} instead
	 */
	@Override
	@Deprecated
	protected Page<T> readPage(TypedQuery<T> query, Pageable pageable, Specification<T> spec) {
		return readPage(query, getDomainClass(), pageable, spec);
	}

	/**
	 * Reads the given {@link TypedQuery} into a {@link Page} applying the given {@link Pageable} and
	 * {@link Specification}.
	 *
	 * @param query must not be {@literal null}.
	 * @param domainClass must not be {@literal null}.
	 * @param spec can be {@literal null}.
	 * @param pageable can be {@literal null}.
	 * @return
	 */
	@Override
	protected <S extends T> Page<S> readPage(TypedQuery<S> query, final Class<S> domainClass, Pageable pageable, final Specification<S> spec) {

		query.setFirstResult(pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		return PageableExecutionUtils.getPage(query.getResultList(), pageable, new TotalSupplier() {

			@Override
			public long get() {
				return executeCountQuery(getCountQuery(spec, domainClass, READ_PERMISSION));
			}
		});
	}

	/**
	 * Creates a new {@link TypedQuery} from the given {@link Specification}.
	 *
	 * @param spec can be {@literal null}.
	 * @param pageable can be {@literal null}.
	 * @return
	 */
	@Override
	protected TypedQuery<T> getQuery(Specification<T> spec, Pageable pageable) {

		Sort sort = pageable == null ? null : pageable.getSort();
		return getQuery(spec, getDomainClass(), sort);
	}

	/**
	 * Creates a new {@link TypedQuery} from the given {@link Specification}.
	 *
	 * @param spec can be {@literal null}.
	 * @param domainClass must not be {@literal null}.
	 * @param pageable can be {@literal null}.
	 * @return
	 */
	@Override
	protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Pageable pageable) {

		Sort sort = pageable == null ? null : pageable.getSort();
		return getQuery(spec, domainClass, sort);
	}

	/**
	 * Creates a {@link TypedQuery} for the given {@link Specification} and {@link Sort}.
	 * 
	 * @param spec can be {@literal null}.
	 * @param sort can be {@literal null}.
	 * @return
	 */
	@Override
	protected TypedQuery<T> getQuery(Specification<T> spec, Sort sort) {
		return getQuery(spec, sort, READ_PERMISSION);
	}

	protected TypedQuery<T> getQuery(Specification<T> spec, Sort sort, String permission) {
		return getQuery(spec, getDomainClass(), sort, permission);
	}

	/**
	 * Creates a {@link TypedQuery} for the given {@link Specification} and {@link Sort}.
	 *
	 * @param spec can be {@literal null}.
	 * @param domainClass must not be {@literal null}.
	 * @param sort can be {@literal null}.
	 * @return
	 */
	@Override
	protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
		return getQuery(spec, domainClass, sort, READ_PERMISSION);
	}

	protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort, String permission) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<S> query = builder.createQuery(domainClass);

		Root<S> root = applySpecificationToCriteria(spec, domainClass, query, permission);
		if (query.getSelection() == null) {
			query.select(root);
		}
		if (sort != null && query.getSelection() instanceof From) {
			query.orderBy(AclQueryUtils.toOrders(sort, (From<?, ?>) query.getSelection(), builder));
		}

		return applyRepositoryMethodMetadata(em.createQuery(query));
	}

	/**
	 * Creates a new count query for the given {@link Specification}.
	 * 
	 * @param spec can be {@literal null}.
	 * @return
	 * @deprecated override {@link #getCountQuery(Specification, Class)} instead
	 */
	@Override
	@Deprecated
	protected TypedQuery<Long> getCountQuery(Specification<T> spec) {
		return getCountQuery(spec, getDomainClass(), READ_PERMISSION);
	}

	/**
	 * Creates a new count query for the given {@link Specification}.
	 *
	 * @param spec can be {@literal null}.
	 * @param domainClass must not be {@literal null}.
	 * @return
	 */
	protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass, String permission) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);

		Root<S> root = applySpecificationToCriteria(spec, domainClass, query, permission);

		if (query.isDistinct()) {
			query.select(builder.countDistinct(root));
		} else {
			query.select(builder.count(root));
		}

		// Remove all Orders the Specifications might have applied
		query.orderBy(Collections.<Order> emptyList());

		return em.createQuery(query);
	}

	/**
	 * Applies the given {@link Specification} to the given {@link CriteriaQuery}.
	 *
	 * @param spec can be {@literal null}.
	 * @param domainClass must not be {@literal null}.
	 * @param query must not be {@literal null}.
	 * @return
	 */
	private <S, U extends T> Root<U> applySpecificationToCriteria(Specification<U> spec, Class<U> domainClass, CriteriaQuery<S> query, String permission) {

		Assert.notNull(query);
		Assert.notNull(domainClass);
		Root<U> root = query.from(domainClass);

		CriteriaBuilder builder = em.getCriteriaBuilder();

		Predicate predicate = spec == null ? null : spec.toPredicate(root, query, builder);

		// Permission specification must be executed AFTER all of the other specifications
		if (aclSpecification != null) {
			Predicate permissionPredicate = aclSpecification.toPredicate(root, query, builder, permission);
			predicate = predicate == null ? permissionPredicate : builder.and(predicate, permissionPredicate);
		}

		if (predicate != null) {
			query.where(predicate);
		}

		return root;
	}

	private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {

		if (metadata == null) {
			return query;
		}

		LockModeType type = metadata.getLockModeType();
		TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);

		applyQueryHints(toReturn);

		return toReturn;
	}

	private void applyQueryHints(Query query) {

		for (Entry<String, Object> hint : getQueryHints().entrySet()) {
			query.setHint(hint.getKey(), hint.getValue());
		}
	}

	/**
	 * Executes a count query and transparently sums up all values returned.
	 * 
	 * @param query must not be {@literal null}.
	 * @return
	 */
	private static Long executeCountQuery(TypedQuery<Long> query) {

		Assert.notNull(query);

		List<Long> totals = query.getResultList();
		Long total = 0L;

		for (Long element : totals) {
			total += element == null ? 0 : element;
		}

		return total;
	}

	@Override
	@Transactional
	public Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Pageable pageable) {
		if (property.isCollectionLike()) {
			return findAll(new PropertySpecification<T>(id, property), pageable);
		}
		return findOne(new PropertySpecification<T>(id, property));
	}

	@Override
	@Transactional
	public Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId) {
		if (property.isCollectionLike()) {
			return findOne(new PropertySpecification<T>(id, property, propertyId));
		}
		return findOne(new PropertySpecification<T>(id, property));
	}

	private class PropertySpecification<S> implements Specification<S> {

		private String propertyName;
		private Serializable ownerId;
		private String ownerIdName;
		private Serializable propertyId = null;

		public PropertySpecification(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property) {
			this.propertyName = property.getName();
			this.ownerId = id;
			this.ownerIdName = property.getOwner().getIdProperty().getName();
		}

		public PropertySpecification(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId) {
			this(id, property);
			this.propertyId = propertyId;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			Join<?, ?> join = root.join(propertyName);
			query.select((Selection) (join));

			Predicate predicate = cb.equal(root.get(ownerIdName), ownerId);
			if (propertyId != null) {
				EntityType<?> et = em.getMetamodel().entity(join.getJavaType());
				SingularAttribute<?, ?> id = et.getId(et.getIdType().getJavaType());
				predicate = cb.and(predicate, cb.equal(join.get((SingularAttribute) id), propertyId));
			}

			return predicate;
		}

	}

	/**
	 * Specification that gives access to the {@link Parameter} instance used to bind the ids for
	 * {@link SimpleJpaRepository#findAll(Iterable)}. Workaround for OpenJPA not binding collections to in-clauses
	 * correctly when using by-name binding.
	 * 
	 * @see <a href= "https://issues.apache.org/jira/browse/OPENJPA-2018?focusedCommentId=13924055"> OPENJPA-2018</a>
	 * @author Oliver Gierke
	 */
	@SuppressWarnings("rawtypes")
	private static final class ByIdsSpecification<T> implements Specification<T> {

		private final JpaEntityInformation<T, ?> entityInformation;

		ParameterExpression<Iterable> parameter;

		public ByIdsSpecification(JpaEntityInformation<T, ?> entityInformation) {
			this.entityInformation = entityInformation;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax. persistence.criteria.Root,
		 * javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
		 */
		@Override
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

			Path<?> path = root.get(entityInformation.getIdAttribute());
			parameter = cb.parameter(Iterable.class);
			return path.in(parameter);
		}
	}

	/**
	 * {@link Specification} that gives access to the {@link Predicate} instance representing the values contained in
	 * the {@link Example}.
	 *
	 * @author Christoph Strobl
	 * @since 1.10
	 * @param <T>
	 */
	private static class ExampleSpecification<T> implements Specification<T> {

		private final Example<T> example;

		/**
		 * Creates new {@link ExampleSpecification}.
		 *
		 * @param example
		 */
		public ExampleSpecification(Example<T> example) {

			Assert.notNull(example, "Example must not be null!");
			this.example = example;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax. persistence.criteria.Root,
		 * javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
		 */
		@Override
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			return QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
		}
	}

}
