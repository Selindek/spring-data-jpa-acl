package com.berrycloud.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclUser;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.jpa.JpaSpecFeature;

public class AclUserPermissionSpecification implements Specification<AclEntity> {

  @Autowired
  private JpaSpecFeature<AclEntity> jpaSpecFeature;

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
  public Predicate toPredicate(Root<AclEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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
      orderList.add(cb.asc(root.get("id")));
      query.orderBy(orderList);
    }

    List<Predicate> predicates = new ArrayList<>();

    predicates.addAll(createSelfPredicates(root, query, cb, userId));
    predicates.addAll(createOwnerPredicates(root, query, cb, userId));
    predicates.addAll(createPermissionPredicates(root, query, cb, userId));

    return cb.or(predicates.toArray(new Predicate[predicates.size()]));
  }

  /**
   * Creates predicates for current user
   */
  private List<Predicate> createSelfPredicates(Root<AclEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    if (AclUser.class.isAssignableFrom(root.getJavaType())) {
      predicates.add(cb.equal(root.get("id"), userId));
    }
    return predicates;
  }

  /**
   * Creates predicates for direct owners defined by {@link AclOwner} annotation
   */
  private List<Predicate> createOwnerPredicates(Root<AclEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(root.getJavaType());
    for (OwnerData ownerData : metaData.getOwnerDataList()) {
      predicates.add(cb.equal(root.get(ownerData.getAttributeName()).get("id"), userId));
    }
    return predicates;
  }

  /**
   * Creates predicates for permissions
   */
  private List<Predicate> createPermissionPredicates(Root<AclEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb, Serializable userId) {
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(root.join("personPermissionOwners", JoinType.LEFT).get("owner").get("id"), userId));
    return predicates;
  }

}
