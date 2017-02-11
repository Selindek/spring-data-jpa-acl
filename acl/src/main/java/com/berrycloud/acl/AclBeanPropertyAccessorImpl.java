package com.berrycloud.acl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.hateoas.Identifiable;

import com.berrycloud.acl.domain.AclEntity;

public class AclBeanPropertyAccessorImpl implements AclBeanPropertyAccessor{

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private AclUserPermissionSpecification aclUserPermissionSpecification;
  
  @Override
  public void setProperty(PersistentProperty<?> property, Object bean, Object value) {
    // TODO Auto-generated method stub
  }

  // Temporary solution. Should work via Jpa
  @Override
  // @Transactional
  public Object getProperty(PersistentProperty<?> property, Object bean) {
    
    Identifiable<Serializable> owner = (Identifiable<Serializable>) bean;
    
    String propertyName = property.getName();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AclEntity<?>> query = (CriteriaQuery<AclEntity<?>>) cb.createQuery(property.getActualType());
    Root<AclEntity<Serializable>> root = (Root<AclEntity<Serializable>>) query.from(property.getActualType());
    Root<?> ownerRoot = query.from(property.getOwner().getType());

    Predicate AclPredicate = aclUserPermissionSpecification.toPredicate(root, query, cb);

    query.select(root).where(cb.and(AclPredicate,cb.equal(ownerRoot.get("id"), owner.getId()),root.in(ownerRoot.join(propertyName))));

    
    TypedQuery<AclEntity<?>> tq = em.createQuery(query);
    
    if (property.isCollectionLike()) {
      return tq.getResultList();
    }
    if (property.isMap()) {
      // no idea what is it
    }
    return tq.getSingleResult();
  }


}
