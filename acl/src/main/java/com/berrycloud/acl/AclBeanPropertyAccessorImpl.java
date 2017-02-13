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

public class AclBeanPropertyAccessorImpl implements AclBeanPropertyAccessor {

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
    CriteriaBuilder cb = em.getCriteriaBuilder();
    String propertyName = property.getName();

    Identifiable<Serializable> owner = (Identifiable<Serializable>) bean;

    CriteriaQuery<Object> query = cb.createQuery(/* property.getActualType() */);

    Root<?> ownerRoot = query.from(property.getOwner().getType());

    query.select(ownerRoot.join(propertyName)/*.on(cb.equal(ownerRoot.get("id"), owner.getId()))*/);// has a WARNIG, but maybe faster
    Predicate aclPredicate = aclUserPermissionSpecification.toPredicate((Root<AclEntity<Serializable>>) ownerRoot, query, cb);
    query.where(cb.and(aclPredicate ,cb.equal(ownerRoot.get("id"), owner.getId()) )); // <- No WARNING, but I think it's slower

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
