
package com.berrycloud.acl.repository;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.io.Serializable;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.util.Assert;

public class BaseAclJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements PropertyRepository {

  Specification<Object> aclSpecification = null;
  EntityManager em;
  private CrudMethodMetadata metadata;

  public BaseAclJpaRepository(Class<T> domainClass, EntityManager em, Specification<T> aclSpecification) {
    this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
  }

  public BaseAclJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.em = entityManager;
  }

  @Override
  public void setRepositoryMethodMetadata(CrudMethodMetadata crudMethodMetadata) {
    super.setRepositoryMethodMetadata(crudMethodMetadata);
    this.metadata = crudMethodMetadata;
  }

  public void setAclSpecification(Specification<Object> aclSpecification) {
    this.aclSpecification = aclSpecification;
  }

  @Override
  public long count() {
    return super.count((Specification<T>) null);
  }

  @Override
  public boolean exists(ID id) {
    return findOne(id) != null;
  }

  @Override
  public T findOne(ID id) {
    return super.findOne(new Specification<T>() {

      @Override
      public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.equal(root.get("id"), id);
      }

    });
  }

  @Override
  public T getOne(ID id) {
    T entity = this.findOne(id);
    if (entity == null) {
      throw new EntityNotFoundException("Unable to find " + getDomainClass().getName() + " with id " + id);
    }
    return entity;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
    return super.getCountQuery(where((Specification<S>) aclSpecification).and(spec), domainClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
    // return super.getQuery(where((Specification<S>) aclSpecification).and(spec), domainClass, sort);

    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<S> query = builder.createQuery(domainClass);

    Root<S> root = applySpecificationToCriteria(where((Specification<S>) aclSpecification).and(spec), domainClass, query);
    if (query.getSelection() == null) {
      query.select(root);
    }
    if (sort != null && query.getSelection() instanceof From) {
      query.orderBy(AclQueryUtils.toOrders(sort, (From<?, ?>) query.getSelection(), builder));
    }

    return applyRepositoryMethodMetadata(em.createQuery(query));
  }

  // Duplicated private methods
  private <S, U extends T> Root<U> applySpecificationToCriteria(Specification<U> spec, Class<U> domainClass, CriteriaQuery<S> query) {

    Assert.notNull(domainClass, "Domain class must not be null!");
    Assert.notNull(query, "CriteriaQuery must not be null!");

    Root<U> root = query.from(domainClass);

    if (spec == null) {
      return root;
    }

    CriteriaBuilder builder = em.getCriteriaBuilder();
    Predicate predicate = spec.toPredicate(root, query, builder);

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

  @Override
  public Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Pageable pageable) {
    if (property.isCollectionLike()) {
      return findAll(new PropertySpecification<T>(id, property), pageable);
    }
    return findOne(new PropertySpecification<T>(id, property));
  }

  @Override
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
  
  // TODO: create all type-safe implementation for propertyQueries
  //
  // @Override
  // public Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId) {
  // try {
  // return getPropertyQuery(id, property, propertyId, (Sort) null).getSingleResult();
  // } catch (NoResultException e) {
  // return null;
  // }
  //
  // }
  //

  // @Override
  // public Object findProperty(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Pageable pageable) {
  // if (property.isCollectionLike()) {
  // //return findAll(new PropertySpecification<T>(id, property), pageable);
  // TypedQuery<?> query = getPropertyQuery(id, property, null, pageable == null ? null : pageable.getSort());
  // return pageable == null ? new PageImpl<T>(query.getResultList())
  // : readPage(query, getDomainClass(), pageable, spec);
  // }
  // return findProperty(id, property, (Serializable)null);
  // }

//  @SuppressWarnings("unchecked")
//  public <P> TypedQuery<P> getPropertyQuery(Serializable id, PersistentProperty<? extends PersistentProperty<?>> property, Serializable propertyId, Sort sort) {
//
//    CriteriaBuilder cb = em.getCriteriaBuilder();
//    CriteriaQuery<P> query = (CriteriaQuery<P>) cb.createQuery(property.getActualType());
//
//    Root<Object> root = (Root<Object>) query.from(getDomainClass());
//    Join<?, P> join = root.join(property.getName());
//    query.select((join));
//
//    Predicate predicate = cb.equal(root.get(property.getOwner().getIdProperty().getName()), id);
//    if (propertyId != null) {
//      EntityType<?> et = em.getMetamodel().entity(join.getJavaType());
//      SingularAttribute<?, ?> pid = et.getId(et.getIdType().getJavaType());
//      predicate = cb.and(predicate, cb.equal(join.get((SingularAttribute<? super P, ?>) pid), propertyId));
//    }
//
//    query.where(cb.and(predicate, aclSpecification.toPredicate(root, query, cb)));
//
//    if (sort != null) {
//      query.orderBy(toOrders(sort, root, cb));
//    }
//
//    return applyRepositoryMethodMetadata(em.createQuery(query));
//  }
}
