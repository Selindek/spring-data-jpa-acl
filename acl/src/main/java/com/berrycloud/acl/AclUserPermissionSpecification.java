package com.berrycloud.acl;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static com.berrycloud.acl.AclUtils.PERMISSION_PREFIX_DELIMITER;
import static com.berrycloud.acl.AclUtils.READ_PERMISSION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.data.ParentData;
import com.berrycloud.acl.data.PermissionLinkData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;
// TODO: use idProperty instead of getId

public class AclUserPermissionSpecification implements Specification<Object> {

	private static Logger LOG = LoggerFactory.getLogger(AclUserPermissionSpecification.class);

	@Autowired
	private AclUserDetailsService<? extends GrantedAuthority> aclUserDetailsService;

	@Autowired
	private AclMetaData aclMetaData;

	/**
	 * Maximum depth of parent-permission checks. It prevents infinite loops and also limits the complexity of the queries
	 */
	@Value("${spring.data.jpa.acl.max-depth:2}")
	private int maxDepth = 2;

	@Override
	public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return toPredicate(root, query, cb, READ_PERMISSION);
	}

	@SuppressWarnings("unchecked")
	public Predicate toPredicate(Root<?> root, CommonAbstractCriteria query, CriteriaBuilder cb, String permission) {

		// Gather the id of current user
		AclUserDetails user = aclUserDetailsService.getCurrentUser();
		if (user == null) {
			LOG.trace("Access denied for NULL user");
			return cb.disjunction();
		}

		// Skip all predicate constructions if the current user is an admin
		if (aclUserDetailsService.isAdmin()) {
			LOG.trace("Access granted for ADMIN user: {}", user.getUsername());
			return cb.conjunction();
		}

		if (!AclEntity.class.isAssignableFrom(root.getJavaType())) {
			LOG.trace("Access granted for non-AclEntity: {}", root.getJavaType());
			return cb.conjunction();
		}

		From<?, ?> from = root;
		if (query instanceof CriteriaQuery) {
			CriteriaQuery<?> cq = (CriteriaQuery<?>) query;
			// If the selection is NOT the root we have to apply all of the predicates to the selection
			Selection<?> selection = cq.getSelection();
			if (selection != null && selection instanceof From) {
				from = (From<Object, Object>) selection;
			}
		}

		LOG.trace("Creating predicates for {}", from.getJavaType());

		return toSubPredicate(from, cb, user.getUserId(), permission, maxDepth);
	}

	private Predicate toSubPredicate(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission, int depth) {
		LOG.trace("Checking {} for '{}' permission", from.getJavaType(), permission);

		List<Predicate> predicates = new ArrayList<>();

		predicates.addAll(createSelfPredicates(from, cb, userId, permission));
		predicates.addAll(createOwnerPredicates(from, cb, userId, permission));
		predicates.addAll(createOwnerGroupPredicates(from, cb, userId, permission));
		predicates.addAll(createPermissionLinkPredicates(from, cb, userId, permission));
		// Adding predicates recursively for parent entities
		if (depth > 0) {
			predicates.addAll(createParentPredicates(from, cb, userId, permission, depth));
		}

		if (predicates.isEmpty()) {
			LOG.trace("No permissions found");
		}
		return cb.or(predicates.toArray(new Predicate[predicates.size()]));
	}

	/**
	 * Creates predicates for current user
	 * 
	 * @param permission
	 */
	private List<Predicate> createSelfPredicates(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission) {
		List<Predicate> predicates = new ArrayList<>();
		if (AclUser.class.isAssignableFrom(from.getJavaType())) {
			if (aclMetaData.getSelfPermissions().hasPermission(permission)) {
				LOG.trace("Adding 'self' predicate for {}", from.getJavaType());
				SingularAttribute<? super Object, ?> idAttribute = aclMetaData.getAclEntityMetaData(from.getJavaType()).getIdAttribute();
				predicates.add(cb.equal(from.get(idAttribute), userId));
			}
		}
		return predicates;
	}

	/**
	 * Creates predicates for direct owners defined by {@link AclOwner} annotation
	 * 
	 * @param permission
	 */
	private List<Predicate> createOwnerPredicates(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission) {
		List<Predicate> predicates = new ArrayList<>();
		AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
		for (OwnerData ownerData : metaData.getOwnerDataList()) {
			if (ownerData.hasPermission(permission)) {
				LOG.trace("Adding 'owner' predicate for {}.{}", from.getJavaType(), ownerData.getPropertyName());
				SingularAttribute<? super Object, ?> idAttribute = aclMetaData.getAclEntityMetaData(ownerData.getPropertyType()).getIdAttribute();
				if (ownerData.isCollection()) {
					predicates.add(cb.equal(from.join(ownerData.getPropertyName(), JoinType.LEFT).get(idAttribute), userId));
				} else {
					predicates.add(cb.equal(from.get(ownerData.getPropertyName()).get(idAttribute), userId));
				}
			}
		}
		return predicates;
	}

	/**
	 * Creates predicates for group of owners defined by {@link AclOwner} annotation on NON-AclUser fields
	 * 
	 * @param permission
	 */
	private List<Predicate> createOwnerGroupPredicates(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission) {
		List<Predicate> predicates = new ArrayList<>();
		AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
		for (OwnerData ownerGroupData : metaData.getOwnerGroupDataList()) {
			if (ownerGroupData.hasPermission(permission)) {
				LOG.trace("Adding 'owner-group' predicate for {}.{}", from.getJavaType(), ownerGroupData.getPropertyName());
				predicates.addAll(createOwnerPredicates(from.join(ownerGroupData.getPropertyName(), JoinType.LEFT), cb, userId, permission));
			}
		}
		return predicates;
	}

	/**
	 * Creates predicates for parent objects defined by {@link AclParent} annotation
	 * 
	 * @param permission
	 * 
	 */
	private List<Predicate> createParentPredicates(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission, int depth) {
		List<Predicate> predicates = new ArrayList<>();
		AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
		for (ParentData parentData : metaData.getParentDataList()) {
			if (parentData.hasPermission(permission)) {
				LOG.trace("Adding 'parent' sub-predicates for {}.{}", from.getJavaType(), parentData.getPropertyName());
				String permissionPrefix = parentData.getPermissionPrefix();
				String parentPermission = permissionPrefix.isEmpty() ? permission : permissionPrefix + PERMISSION_PREFIX_DELIMITER + permission;
				predicates.add(toSubPredicate(from.join(parentData.getPropertyName(), JoinType.LEFT), cb, userId, parentPermission, depth - 1));
			}
		}
		return predicates;
	}

	/**
	 * Creates predicates for permissionLinks
	 *
	 */
	private List<Predicate> createPermissionLinkPredicates(From<?, ?> from, CriteriaBuilder cb, Serializable userId, String permission) {
		List<Predicate> predicates = new ArrayList<>();

		AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
		for (PermissionLinkData permissionLinkData : metaData.getPermissionLinkList()) {
			LOG.trace("Adding 'permission-link' predicate for {}.{}", from.getJavaType(), permissionLinkData.getPropertyName());
			Join<Object, Object> permissionLink = from.join(permissionLinkData.getPropertyName(), JoinType.LEFT);
			permissionLink.on(createOnPredicate(cb, permissionLink.<String> get(permissionLinkData.getPermissionField()), permission));
			predicates.addAll(createOwnerPredicates(permissionLink, cb, userId, permission));
		}

		return predicates;
	}

	private Predicate createOnPredicate(CriteriaBuilder cb, Expression<String> field, String permission) {
		int index = permission.lastIndexOf(PERMISSION_PREFIX_DELIMITER) + 1;

		List<Predicate> onPredicates = createOnPredicate(cb, field, permission.substring(index), permission.substring(0, index));
		return cb.or(onPredicates.toArray(new Predicate[onPredicates.size()]));
	}

	private List<Predicate> createOnPredicate(CriteriaBuilder cb, Expression<String> field, String permission, String prefixes) {
		List<Predicate> onPredicates = new ArrayList<>();
		if (prefixes.isEmpty()) {
			// Create top level permission-checks
			if (permission.equals(READ_PERMISSION)) {
				onPredicates.add(cb.notLike(field, prefixes + "%" + PERMISSION_PREFIX_DELIMITER + "%"));
			} else {
				onPredicates.add(cb.equal(field, ALL_PERMISSION));
				onPredicates.add(cb.equal(field, permission));
			}
		} else {
			// Create child-level permission-checks
			if (permission.equals(READ_PERMISSION)) {
				onPredicates.add(cb.like(field, prefixes + "%"));
			} else {
				onPredicates.add(cb.equal(field, prefixes + ALL_PERMISSION));
				onPredicates.add(cb.equal(field, prefixes + permission));
			}

			// create permission-checks recursively for upper child-levels
			int index = prefixes.lastIndexOf(PERMISSION_PREFIX_DELIMITER, prefixes.length() - 2) + 1;
			onPredicates.addAll(createOnPredicate(cb, field, permission, prefixes.substring(0, index)));

		}

		return onPredicates;
	}

}
