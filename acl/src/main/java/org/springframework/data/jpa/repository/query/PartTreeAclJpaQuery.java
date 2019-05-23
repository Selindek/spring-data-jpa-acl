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
package org.springframework.data.jpa.repository.query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.DeleteExecution;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ExistsExecution;
import org.springframework.data.jpa.repository.query.ParameterMetadataProvider.ParameterMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;

import com.berrycloud.acl.AclSpecification;
import com.berrycloud.acl.search.Search;

/**
 * This class is an extension of the {@link PartTreeJpaQuery} class with some addition for the Acl. The whole source was
 * copy-pasted and the changes was injected directly into the copied source.
 *
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Jens Schauder
 * @author Mark Paluch
 * @author ĐˇĐµŃ€ĐłĐµĐą Đ¦Ń‹ĐżĐ°Đ˝ĐľĐ˛
 * @author István Rátkai (Selindek)
 */
public class PartTreeAclJpaQuery extends AbstractJpaQuery {

  private final PartTree tree;
  private final JpaParameters parameters;

  private final QueryPreparer query;
  private final QueryPreparer countQuery;
  private final EntityManager em;
  private final EscapeCharacter escape;
  
  private final AclSpecification aclSpecification;

  /**
   * Creates a new {@link PartTreeJpaQuery}.
   * 
   * @param method
   *          must not be {@literal null}.
   * @param em
   *          must not be {@literal null}.
   * @param persistenceProvider
   *          must not be {@literal null}.
   */
  PartTreeAclJpaQuery(JpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, EscapeCharacter escape,
      AclSpecification aclSpecification) {

    super(method, em);

    this.em = em;
    this.escape = escape;
    Class<?> domainClass = method.getEntityInformation().getJavaType();
    this.parameters = method.getParameters();
    this.aclSpecification = aclSpecification;

    try {

      this.tree = new PartTree(method.getName(), domainClass);
      this.countQuery = new CountQueryPreparer(persistenceProvider);
      this.query = tree.isCountProjection() ? countQuery : new QueryPreparer(persistenceProvider);

    } catch (Exception o_O) {
      throw new IllegalArgumentException(
          String.format("Failed to create query for method %s! %s", method, o_O.getMessage()), o_O);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateQuery(java.lang.Object[])
   */
  @Override
  public Query doCreateQuery(Object[] values) {
    return query.createQuery(values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateCountQuery(java.lang.Object[])
   */
  @Override
  @SuppressWarnings("unchecked")
  public TypedQuery<Long> doCreateCountQuery(Object[] values) {
    return (TypedQuery<Long>) countQuery.createQuery(values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#getExecution()
   */
  @Override
  protected JpaQueryExecution getExecution() {

    if (this.tree.isDelete()) {
      return new DeleteExecution(em);
    } else if (this.tree.isExistsProjection()) {
      return new ExistsExecution();
    }

    return super.getExecution();
  }

  /**
   * Query preparer to create {@link CriteriaQuery} instances and potentially cache them.
   * 
   * @author Oliver Gierke
   * @author Thomas Darimont
   * @author István Rátkai (Selindek)
   */
  private class QueryPreparer {

    private final PersistenceProvider persistenceProvider;

    QueryPreparer(PersistenceProvider persistenceProvider) {

      this.persistenceProvider = persistenceProvider;
    }

    /**
     * Creates a new {@link Query} for the given parameter values.
     */
    public Query createQuery(Object[] values) {

      ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

      JpaQueryCreator creator = createCreator(persistenceProvider, Optional.of(accessor));
      Sort sort = getDynamicSort(values);
      CriteriaQuery<?> criteriaQuery = creator.createQuery(sort);
      List<ParameterMetadata<?>> expressions = creator.getParameterExpressions();
      ParameterBinder parameterBinder = getBinder(expressions);

      applyAcl(criteriaQuery);

      if (sort instanceof Search) {
        aclSpecification.applySearch(criteriaQuery, em.getCriteriaBuilder(), criteriaQuery.getRoots().iterator().next(),
            (Search) sort);
        if (creator instanceof JpaCountQueryCreator) {
          criteriaQuery.orderBy(Collections.<Order> emptyList());
        }
      }

      if (parameterBinder == null) {
        throw new IllegalStateException("ParameterBinder is null!");
      }

      return restrictMaxResultsIfNecessary(invokeBinding(parameterBinder, createQuery(criteriaQuery), values));
    }

    private void applyAcl(CriteriaQuery<?> criteriaQuery) {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      @SuppressWarnings("unchecked")
      Root<Object> root = (Root<Object>) criteriaQuery.getRoots().iterator().next();
      Predicate original = criteriaQuery.getRestriction();
      Predicate acl = aclSpecification.toPredicate(root, criteriaQuery, cb);

      criteriaQuery.where(original == null ? acl : cb.and(original, acl));
    }

    /**
     * Restricts the max results of the given {@link Query} if the current {@code tree} marks this {@code query} as
     * limited.
     */
    private Query restrictMaxResultsIfNecessary(Query query) {

      if (tree.isLimiting()) {

        if (query.getMaxResults() != Integer.MAX_VALUE) {
          /*
           * In order to return the correct results, we have to adjust the first result offset to be returned if: - a
           * Pageable parameter is present - AND the requested page number > 0 - AND the requested page size was bigger
           * than the derived result limitation via the First/Top keyword.
           */
          if (query.getMaxResults() > tree.getMaxResults() && query.getFirstResult() > 0) {
            query.setFirstResult(query.getFirstResult() - (query.getMaxResults() - tree.getMaxResults()));
          }
        }

        query.setMaxResults(tree.getMaxResults());
      }

      if (tree.isExistsProjection()) {
        query.setMaxResults(1);
      }

      return query;
    }

    private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) {

      return getEntityManager().createQuery(criteriaQuery);
    }

    protected JpaQueryCreator createCreator(PersistenceProvider persistenceProvider,
        Optional<ParametersParameterAccessor> accessor) {

      EntityManager entityManager = getEntityManager();
      CriteriaBuilder builder = entityManager.getCriteriaBuilder();

      ParameterMetadataProvider provider = accessor
          .map(it -> new ParameterMetadataProvider(builder, it, persistenceProvider, escape))//
          .orElseGet(() -> new ParameterMetadataProvider(builder, parameters, persistenceProvider, escape));

      ResultProcessor processor = getQueryMethod().getResultProcessor();
      ReturnedType returnedType = accessor.map(processor::withDynamicProjection)//
          .orElse(processor).getReturnedType();

      return new JpaQueryCreator(tree, returnedType, builder, provider);
    }

    /**
     * Invokes parameter binding on the given {@link TypedQuery}.
     */
    protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query, Object[] values) {

      return binder.bindAndPrepare(query, values);
    }

    private ParameterBinder getBinder(List<ParameterMetadata<?>> expressions) {
      return ParameterBinderFactory.createCriteriaBinder(parameters, expressions);
    }

    private Sort getDynamicSort(Object[] values) {

      return parameters.potentiallySortsDynamically() //
          ? new ParametersParameterAccessor(parameters, values).getSort() //
          : Sort.unsorted();
    }
  }

  /**
   * Special {@link QueryPreparer} to create count queries.
   * 
   * @author Oliver Gierke
   * @author Thomas Darimont
   */
  private class CountQueryPreparer extends QueryPreparer {

    CountQueryPreparer(PersistenceProvider persistenceProvider) {
      super(persistenceProvider);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.data.jpa.repository.query.PartTreeJpaQuery.QueryPreparer#createCreator(org.
     * springframework.data.repository.query.ParametersParameterAccessor,
     * org.springframework.data.jpa.provider.PersistenceProvider)
     */
    @Override
    protected JpaQueryCreator createCreator(PersistenceProvider persistenceProvider,
        Optional<ParametersParameterAccessor> accessor) {

      EntityManager entityManager = getEntityManager();
      CriteriaBuilder builder = entityManager.getCriteriaBuilder();

      ParameterMetadataProvider provider = accessor
          .map(it -> new ParameterMetadataProvider(builder, it, persistenceProvider, escape))//
          .orElseGet(() -> new ParameterMetadataProvider(builder, parameters, persistenceProvider, escape));

      return new JpaCountQueryCreator(tree, getQueryMethod().getResultProcessor().getReturnedType(), builder, provider);
    }

    /**
     * Customizes binding by skipping the pagination.
     * 
     * @see QueryPreparer#invokeBinding(ParameterBinder, TypedQuery, Object[])
     */
    @Override
    protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query, Object[] values) {
      return binder.bind(query, values);
    }
  }
}
