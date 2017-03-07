package com.berrycloud.acl.repository;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.AclJpaQueryLookupStrategy;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import com.berrycloud.acl.AclUserPermissionSpecification;

public class AclJpaRepositoryFactory extends JpaRepositoryFactory {

    private AclUserPermissionSpecification aclSpecification;
    private EntityManager entityManager;
    private final QueryExtractor extractor;

    public AclJpaRepositoryFactory(EntityManager entityManager, AclUserPermissionSpecification aclSpecification) {
        super(entityManager);
        this.aclSpecification = aclSpecification;
        this.entityManager = entityManager;
        this.extractor = PersistenceProvider.fromEntityManager(entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleAclJpaRepository.class;
    }

    protected boolean isAclRepository(RepositoryMetadata metadata) {
        NoAcl noAcl = metadata.getRepositoryInterface().getDeclaredAnnotation(NoAcl.class);
        return noAcl == null;
    }

    @Override
    protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information,
            EntityManager entityManager) {
        SimpleJpaRepository<?, ?> repository = super.getTargetRepository(information, entityManager);
        ((SimpleAclJpaRepository<?, ?>) repository)
                .setAclSpecification(isAclRepository(information) ? aclSpecification : null);
        return repository;
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
        return AclJpaQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider,
                aclSpecification);
    }

}
