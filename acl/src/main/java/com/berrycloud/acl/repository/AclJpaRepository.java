
package com.berrycloud.acl.repository;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class AclJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> {

  Specification<?> aclSpecification = null;

  public AclJpaRepository(Class<T> domainClass, EntityManager em, Specification<T> aclSpecification) {
    this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
  }

  public AclJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  public void setAclSpecification(Specification<?> aclSpecification) {
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
    return super.getQuery(where((Specification<S>) aclSpecification).and(spec), domainClass, sort);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + getDomainClass().getSimpleName() + ">";
  }
}
