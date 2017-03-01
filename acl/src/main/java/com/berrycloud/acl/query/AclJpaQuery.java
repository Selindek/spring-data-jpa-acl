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
package com.berrycloud.acl.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.berrycloud.acl.AclUserPermissionSpecification;

public class AclJpaQuery implements RepositoryQuery {

	private static Logger LOG = LoggerFactory.getLogger(AclJpaQuery.class);
	private RepositoryQuery query;
	private EntityManager em;
	private Method method;
	private CriteriaQuery<?> cachedCriteriaQuery;
	private AclPredicateTargetSource aclPredicateTargetSource;
	private Root<Object> root;
	private AclUserPermissionSpecification aclSpecification;

	public AclJpaQuery(Method method, RepositoryQuery query, EntityManager em, AclUserPermissionSpecification aclSpecification) {
		this.method = method;
		this.query = query;
		this.em = em;
		this.aclSpecification = aclSpecification;
		installAclProxy(query);
	}

	@Override
	public Object execute(Object[] parameters) {
		if (cachedCriteriaQuery == null) {
			return query.execute(parameters);
		}

		synchronized (cachedCriteriaQuery) {
			installAclSpec(aclSpecification);
			try {
				return query.execute(parameters);
			} finally {
				uninstallAclSpec();
			}
		}
	}

	private void installAclSpec(Specification<Object> aclSpecification) {
		// force rerender by resetting alias
		root.alias(null);

		// TODO handle different permissions (from annotation maybe?)
		Predicate aclPredicate = aclSpecification.toPredicate(root, cachedCriteriaQuery, em.getCriteriaBuilder());

		aclPredicateTargetSource.installAcl(aclPredicate);

		LOG.debug("ACL Specification installed for method '{}'", method);
	}

	private void uninstallAclSpec() {
		if (aclPredicateTargetSource != null) {
			aclPredicateTargetSource.uninstallAcl();
			LOG.debug("ACL Specification uninstalled from method '{}'", method);
		}
	}

	private void installAclProxy(RepositoryQuery query) {
		cachedCriteriaQuery = criteriaQuery();
		if (cachedCriteriaQuery == null) {
			LOG.error("Unable to install ACL Jpa Specification for method '{}'. Query methods with Pageable/Sort are not (yet) supported", method);
			return;
		}
		this.root = root(cachedCriteriaQuery);

		try {
			this.aclPredicateTargetSource = installAclPredicateTargetSource();
		} catch (Exception e) {
			LOG.error("Unable to install ACL Jpa Specification for method '{}'", method, e);
		}
	}

	private AclPredicateTargetSource installAclPredicateTargetSource() {
		synchronized (cachedCriteriaQuery) {
			Predicate restriction = cachedCriteriaQuery.getRestriction();

			if (restriction instanceof Advised) {
				Advised advised = (Advised) restriction;
				if (advised.getTargetSource() instanceof AclPredicateTargetSource) {
					return (AclPredicateTargetSource) advised.getTargetSource();
				}
			}

			AclPredicateTargetSource targetSource = new AclPredicateTargetSource(em.getCriteriaBuilder(), restriction);
			ProxyFactoryBean factoryBean = new ProxyFactoryBean();
			factoryBean.setTargetSource(targetSource);
			factoryBean.setAutodetectInterfaces(true);
			Predicate enhancedPredicate = (Predicate) factoryBean.getObject();
			LOG.debug("ACL Jpa Specification target source initialized for criteria {}", cachedCriteriaQuery);

			// install proxy inside criteria
			cachedCriteriaQuery.where(enhancedPredicate);
			return targetSource;
		}
	}

	@Override
	public QueryMethod getQueryMethod() {
		return query.getQueryMethod();
	}

	private CriteriaQuery<?> criteriaQuery() {
		Object queryPreparer = getField(PartTreeJpaQuery.class, query, "query");
		CriteriaQuery<?> criteriaQuery = getField(queryPreparer.getClass(), queryPreparer, "cachedCriteriaQuery");
		return criteriaQuery;
	}

	@SuppressWarnings("unchecked")
	private Root<Object> root(CriteriaQuery<?> criteriaQuery) {
		return (Root<Object>) criteriaQuery.getRoots().iterator().next();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getField(Class<?> type, Object object, String fieldName) {
		Field field = ReflectionUtils.findField(type, fieldName);
		field.setAccessible(true);
		Object property = ReflectionUtils.getField(field, object);
		return (T) property;
	}

}
