package com.berrycloud.acl.security.access;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import com.berrycloud.acl.AclUserPermissionSpecification;

public class AclPermissionEvaluator implements PermissionEvaluator {

    private static Logger LOG = LoggerFactory.getLogger(AclPermissionEvaluator.class);

    // Dynamically filled cache for entityInformation
    private final Map<Class<?>, JpaEntityInformation<?, ?>> entityInformationMap = new HashMap<>();

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AclUserPermissionSpecification aclSpecification;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject == null) {
            return false;
        }
        try {
            Class<?> domainClass = targetDomainObject.getClass();
            return hasPermission(authentication, getId(targetDomainObject), domainClass, permission);
        } catch (Exception ex) {
            LOG.warn("Invalid target for AclPermissionEvaluator: {}", targetDomainObject);
            LOG.trace("Details: ", ex);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        try {
            return hasPermission(authentication, targetId, Class.forName(targetType), permission);
        } catch (ClassNotFoundException ex) {
            LOG.warn("Invalid target type for AclPermissionEvaluator: {}", targetType);
            LOG.trace("Details: ", ex);
            return false;
        }
    }

    /**
     * Check permission by directly creating a JPA count query with ACL support for the given permission
     */
    public <T, ID extends Serializable> boolean hasPermission(Authentication authentication, ID targetId,
            Class<T> domainClass, Object permission) {
        String permissionString = getPermissionString(permission);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<T> root = query.from(domainClass);
        query.select(builder.count(root));

        Predicate idPredicate = builder.equal(root.get(getEntityInformation(domainClass).getIdAttribute()), targetId);
        Predicate aclPredicate = aclSpecification.toPredicate(root, query, builder, permissionString);
        query.where(builder.and(idPredicate, aclPredicate));

        try {
            return em.createQuery(query).getSingleResult() != 0;
        } catch (PersistenceException e) {
            return false;
        }

    }

    protected <T> Serializable getId(T object) {
        @SuppressWarnings("unchecked")
        Class<T> domainClass = (Class<T>) object.getClass();
        JpaEntityInformation<T, ?> entityInformation = getEntityInformation(domainClass);
        if (entityInformation == null) {
            throw new IllegalArgumentException("Not a valid JPA entity class: " + domainClass);
        }
        Object id = entityInformation.getId(object);
        if (id instanceof Serializable) {
            return (Serializable) id;
        }
        throw new IllegalArgumentException("Id is not Serializable for " + domainClass);
    }

    protected <T> JpaEntityInformation<T, ?> getEntityInformation(Class<T> domainClass) {
        @SuppressWarnings("unchecked")
        JpaEntityInformation<T, ?> entityInformation = (JpaEntityInformation<T, ?>) entityInformationMap
                .get(domainClass);
        if (entityInformation == null) {
            entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, em);
            entityInformationMap.put(domainClass, entityInformation);
        }
        return entityInformation;
    }

    protected String getPermissionString(Object permission) {
        return permission.toString();
    }

}
