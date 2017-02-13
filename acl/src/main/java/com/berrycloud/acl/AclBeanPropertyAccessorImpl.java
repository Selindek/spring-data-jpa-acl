package com.berrycloud.acl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
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
    CriteriaQuery<Object> query =  (CriteriaQuery<Object>) cb.createQuery(property.getActualType());
    
    //Root<?> root =  query.from(property.getActualType());
    Root<AclEntity<Serializable>> ownerRoot = (Root<AclEntity<Serializable>>) query.from(property.getOwner().getType());
    //Predicate aclPredicate = aclUserPermissionSpecification.toPredicate(root, query, cb);
    //query.select(root).where(cb.and(aclPredicate,cb.equal(ownerRoot.get("id"), owner.getId()),root.in(ownerRoot.join(propertyName))));

    From<Root<AclEntity<Serializable>>,Root<AclEntity<Serializable>>> join = ownerRoot.join(propertyName);
    //((Join)join).on(cb.equal(ownerRoot.get("id"), owner.getId()));
    query.select(join);
    Predicate aclPredicate = aclUserPermissionSpecification.toPredicate(ownerRoot, query, cb);
    query.where(cb.equal(ownerRoot.get("id"), owner.getId()));
    query.distinct(true);
    
    TypedQuery<?> tq = em.createQuery(query);
    
    if (property.isCollectionLike()) {
      return tq.getResultList();
    }
    if (property.isMap()) {
      // no idea what is it
    }
    return tq.getSingleResult();
  }


}
