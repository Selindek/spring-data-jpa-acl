/*******************************************************************************
 * Copyright 2002-2016 the original author or authors.
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
 *******************************************************************************/
package com.berrycloud.acl.test;

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

  Specification<?> aclSpecification=null;

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
