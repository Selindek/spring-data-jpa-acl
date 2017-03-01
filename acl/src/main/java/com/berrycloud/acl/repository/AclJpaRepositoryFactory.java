package com.berrycloud.acl.repository;

import static org.springframework.util.ReflectionUtils.findMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;

import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.query.AclJpaQuery;

public class AclJpaRepositoryFactory extends JpaRepositoryFactory {

	private static Logger LOG = LoggerFactory.getLogger(AclJpaRepositoryFactory.class);

	public AclUserPermissionSpecification aclSpecification;
	private EntityManager entityManager;

	public AclJpaRepositoryFactory(EntityManager entityManager, AclUserPermissionSpecification aclSpecification) {
		super(entityManager);
		this.aclSpecification = aclSpecification;
		this.entityManager = entityManager;
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleAclJpaRepository.class;
	}

	protected boolean isAclRepository(RepositoryMetadata metadata) {
		NoAcl noAcl = metadata.getRepositoryInterface().getDeclaredAnnotation(NoAcl.class);
		return AclEntity.class.isAssignableFrom(metadata.getDomainType()) && noAcl == null;
	}

	protected boolean isAclMethod(RepositoryMetadata metadata, Method method) {
		NoAcl noAcl = method.getDeclaredAnnotation(NoAcl.class);
		return isAclRepository(metadata) && noAcl == null;
	}

	@Override
	protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
		SimpleJpaRepository<?, ?> repository = super.getTargetRepository(information, entityManager);
		((SimpleAclJpaRepository<?, ?>) repository).setAclSpecification(isAclRepository(information) ? aclSpecification : null);
		return repository;
	}

	@Override
	protected QueryLookupStrategy getQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
		return new AclQueryLookupStrategy(key, evaluationContextProvider);
	}

	private class AclQueryLookupStrategy implements QueryLookupStrategy {
		private Key key;
		private EvaluationContextProvider evaluationContextProvider;

		public AclQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
			this.key = key;
			this.evaluationContextProvider = evaluationContextProvider;
		}

		/**
		 * Compatibility for projects using Spring Data JPA < 1.10.0
		 */
		@SuppressWarnings("unused")
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
			QueryLookupStrategy queryLookupStrategy = AclJpaRepositoryFactory.super.getQueryLookupStrategy(key, evaluationContextProvider);

			Method resolveQuery = findMethod(QueryLookupStrategy.class, "resolveQuery", Method.class, RepositoryMetadata.class, NamedQueries.class);
			try {
				RepositoryQuery query = (RepositoryQuery) resolveQuery.invoke(queryLookupStrategy, method, metadata, namedQueries);
				return wrapQuery(method, metadata, query);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * @since Spring data JPA 1.10.0
		 */
		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
			QueryLookupStrategy queryLookupStrategy = AclJpaRepositoryFactory.super.getQueryLookupStrategy(key, evaluationContextProvider);

			RepositoryQuery query = queryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
			return wrapQuery(method, metadata, query);
		}

		private RepositoryQuery wrapQuery(Method method, RepositoryMetadata metadata, RepositoryQuery query) {
			if (isAclMethod(metadata, method)) {
				if (query instanceof PartTreeJpaQuery) {
					return new AclJpaQuery(method, query, entityManager, aclSpecification);
				} else {
					LOG.error("Unsupported query type for method '{}'. ACL Specification was NOT installed.", method);
				}
			}
			return query;
		}
	}
}
