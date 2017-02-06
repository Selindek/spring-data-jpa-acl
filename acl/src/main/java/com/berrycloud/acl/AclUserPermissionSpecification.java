package com.berrycloud.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclUser;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.jpa.JpaSpecFeature;

public class AclUserPermissionSpecification implements Specification<AclEntity<Serializable>> {

  @Autowired
  private JpaSpecFeature<AclEntity<Serializable>> jpaSpecFeature;

  @Autowired
  private SimpleAclStrategy aclUserStrategy;

  @Autowired
  private AclLogic aclLogic;

  @Autowired
  private AclMetaData aclMetaData;

  @PostConstruct
  public void init() {
    aclUserStrategy.install(jpaSpecFeature, this);
  }

  @Override
  public Predicate toPredicate(Root<AclEntity<Serializable>> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return toInnerPredicate(root, query, cb);
  }
  
  protected Predicate toInnerPredicate(From<AclEntity<Serializable>,AclEntity<Serializable>> from, CriteriaQuery<?> query, CriteriaBuilder cb) {
    // Skip all predicate constructions if the current user is an admin
    if (aclLogic.isAdmin()) {
      return cb.conjunction();
    }

    // Gather the id of current user
    Serializable userId = aclLogic.getCurrentUser().getUserId();

    query.distinct(true);

    // TODO check id sorting
    List<Order> orderList = new ArrayList<>(query.getOrderList());
    if (orderList.isEmpty()) {
      // Need ordering for pagination
      orderList.add(cb.asc(from.get("id")));
      query.orderBy(orderList);
    }

    List<Predicate> predicates = new ArrayList<>();

    predicates.addAll(createSelfPredicates(from, query, cb, userId));
    predicates.addAll(createOwnerPredicates(from, query, cb, userId));
    predicates.addAll(createParentPredicates(from, query, cb, userId));
    predicates.addAll(createPermissionPredicates(from, query, cb, userId));

    return cb.or(predicates.toArray(new Predicate[predicates.size()]));
  }

  /**
   * Creates predicates for current user
   */
  private List<Predicate> createSelfPredicates(From<AclEntity<Serializable>,AclEntity<Serializable>> from, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    if (AclUser.class.isAssignableFrom(from.getJavaType())) {
      predicates.add(cb.equal(from.get("id"), userId));
    }
    return predicates;
  }

  /**
   * Creates predicates for direct owners defined by {@link AclOwner} annotation
   */
  private List<Predicate> createOwnerPredicates(From<AclEntity<Serializable>,AclEntity<Serializable>> from, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (OwnerData ownerData : metaData.getOwnerDataList()) {
      predicates.add(cb.equal(from.get(ownerData.getAttributeName()).get("id"), userId));
    }
    return predicates;
  }
  
  /**
   * Creates predicates for parent objects defined by {@link AclParent} annotation
   * 
   */
  private List<Predicate> createParentPredicates(From<AclEntity<Serializable>,AclEntity<Serializable>> from, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (String parent: metaData.getParentList()) {
      predicates.add(toInnerPredicate(from.join(parent,JoinType.LEFT), query, cb));
    }
    return predicates;
  }

  /**
   * Creates predicates for permissions
   */
  private List<Predicate> createPermissionPredicates(From<AclEntity<Serializable>,AclEntity<Serializable>> from, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for(Class<?> ownerPermissionClass: metaData.getOwnerPermissionList()) {
      Root<?> permissionLink = query.from(ownerPermissionClass);
      predicates.add(cb.and(cb.equal(permissionLink.get("target"),from.get("id")),cb.equal(permissionLink.get("owner"),userId)));
    }

    return predicates;
  }

}
