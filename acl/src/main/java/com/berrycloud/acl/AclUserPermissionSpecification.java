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
import org.springframework.security.core.context.SecurityContextHolder;

import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.security.SimpleAclUserDetails;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.jpa.JpaSpecFeature;

public class AclUserPermissionSpecification implements Specification<AclEntity> {

    @Autowired
    private JpaSpecFeature<AclEntity> jpaSpecFeature;

    @Autowired
    private SimpleAclStrategy aclUserStrategy;

//    @Autowired
//    private AclLogic aclLogic;

    @Autowired
    private AclMetaData aclMetaData;

    @PostConstruct
    public void init() {
	aclUserStrategy.install(jpaSpecFeature, this);
    }

    @Override
    public Predicate toPredicate(Root<AclEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
	// Gather id of current user
	Serializable userId = ((SimpleAclUserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
		.getUserId();

	query.distinct(true);

	// Add an extra ordering to the query
	List<Order> orderList = new ArrayList<>(query.getOrderList());
	orderList.add(cb.asc(root.get("id")));
	query.orderBy(orderList);
	
	// Create permission predicates
	Root<? extends AclUser<Serializable, AclRole<Serializable>>> currentUser = query.from(aclMetaData.getAclUserType());
	
	Predicate admin = cb.and(cb.equal(currentUser.get("id"), userId),
		cb.equal(currentUser.join("aclRoles", JoinType.LEFT).get("roleName"), "ROLE_ADMIN"));
	Predicate self = cb.equal(root.get("id"), userId);
	Predicate creator = cb.equal(root.get("createdBy").get("id"), userId);
	Predicate personPermission = cb
		.equal(root.join("personPermissionOwners", JoinType.LEFT).get("owner").get("id"), userId);

	return cb.or(self, creator, personPermission, admin);
    }

}
