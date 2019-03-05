/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.berrycloud.acl;

import static com.berrycloud.acl.AclConstants.ALL_PERMISSION;
import static com.berrycloud.acl.AclConstants.CREATE_PERMISSION;
import static com.berrycloud.acl.AclConstants.PERMISSION_PREFIX_DELIMITER;
import static com.berrycloud.acl.AclConstants.READ_PERMISSION;

import java.util.ArrayList;
import java.util.List;

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

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.CreatePermissionData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.data.ParentData;
import com.berrycloud.acl.data.PermissionLinkData;
import com.berrycloud.acl.data.RolePermissionData;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.search.Search;
import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;

/**
 * Implementation of the {@link AclSpecification}.
 *
 * @author István Rátkai (Selindek)
 */
public class AclUserPermissionSpecification implements AclSpecification {

  private static final long serialVersionUID = 920905870030120482L;

  private static Logger LOG = LoggerFactory.getLogger(AclUserPermissionSpecification.class);

  @Autowired
  private AclMetaData aclMetaData;

  /**
   * Maximum depth of parent-permission checks. It prevents infinite loops and also limits the complexity of the queries
   */
  @Value("${spring.data.jpa.acl.max-depth:2}")
  private int maxDepth = 2;

  /**
   * Maximum number of words which are processed in a pattern
   */
  @Value("${spring.data.jpa.acl.max-search-words:3}")
  private int maxWords = 3;

  @Override
  public void applySearch(CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb, From<?, ?> from, Search search) {

    final List<String> properties = aclMetaData.getAclEntityMetaData(from.getJavaType()).getSearchableAttributes();
    if (properties.isEmpty()) {
      return;
    }

    float patternNum = 1f;
    Expression<Number> order = null;

    for (final String pattern : search.getPatterns()) {

      final Float weight = pattern.length() / patternNum;
      Expression<Number> patternOrder = null;

      for (final String p : properties) {
        final Expression<Number> value = cb
            .coalesce(cb.quot(weight, cb.nullif(cb.locate(cb.lower(from.get(p)), pattern, 1), 0)), 0f);
        patternOrder = patternOrder == null ? value : cb.sum(patternOrder, value);
      }

      // If a pattern cannot be found at all then invalidate the whole order by setting the current order to null
      patternOrder = cb.nullif(patternOrder, 0f);
      // Sum the order value of all patterns
      order = order == null ? patternOrder : cb.sum(order, patternOrder);

      if (++patternNum > maxWords) {
        break;
      }
    }

    if (order == null) {
      return;
    }

    // add the search predicate to the query
    Predicate predicate = cb.gt(order, 0f);
    Predicate original = criteriaQuery.getRestriction();

    criteriaQuery.where(original == null ? predicate : cb.and(original, predicate));

    if (criteriaQuery.isDistinct()) {
      criteriaQuery.distinct(false);
      criteriaQuery.groupBy((Expression<?>) criteriaQuery.getSelection());
      order = cb.max(order);
    }

    // override possible previous ordering with the search order
    // also add the id as a secondary order to make sure we get the same
    // ordering each time. (mandatory for pagination)
    criteriaQuery.orderBy(cb.desc(order),
        cb.asc(from.get(aclMetaData.getAclEntityMetaData(from.getJavaType()).getIdAttribute())));
  }

  @Override
  public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    return toPredicate(root, query, cb, READ_PERMISSION);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb, String permission) {

    From<?, ?> from = root;
    // If the selection is NOT the root we have to apply all of the predicates to the selection
    Selection<?> selection = query.getSelection();
    if (selection != null && selection instanceof From) {
      from = (From<Object, Object>) selection;
    }

    // Rules from @AclRolePermission annotations
    if (hasRolePermission(from, permission)) {
      LOG.trace("Access granted via @AclRolePermission: {}", AclUserDetailsService.getUsername());
      return cb.conjunction();
    }

    // Rules from @AclRoleCondition annotations
    if (!hasRoleCondition(from, permission)) {
      LOG.trace("Access denied via @AclRoleCondition: {}", AclUserDetailsService.getUsername());
      return cb.disjunction();
    }

    // Gather the UserDetails of the current user
    AclUserDetails aclUserDetails = AclUserDetailsService.getAclUserDetails();
    if (aclUserDetails == null) {
      LOG.trace("Access denied for non-Acl user");
      return cb.disjunction();
    }

    LOG.trace("Creating predicates for {}", from.getJavaType());

    query.distinct(true);
    return toSubPredicate(from, cb, aclUserDetails.getUserId(), permission, maxDepth);
  }

  private Predicate toSubPredicate(From<?, ?> from, CriteriaBuilder cb, Object userId, String permission, int depth) {
    LOG.trace("Checking {} for '{}' permission", from.getJavaType(), permission);

    List<Predicate> predicates = new ArrayList<>();

    predicates.addAll(createSelfPredicates(from, cb, userId, permission));
    predicates.addAll(createOwnerPredicates(from, cb, userId, permission, false));
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
   * Checks if the current user has any role which grants automatic permission for this domain type.
   *
   * @param from
   * @param permission
   * @return
   */
  private boolean hasRolePermission(From<?, ?> from, String permission) {
    return hasRolePermission(aclMetaData.getAclEntityMetaData(from.getJavaType()), permission);
  }

  /**
   * Checks if the current user has any role which grants automatic permission for this domain type.
   * 
   * @param javaType
   * @param permission
   * @return
   */
  private boolean hasRolePermission(AclEntityMetaData metaData, String permission) {
    for (RolePermissionData rolePermissionData : metaData.getRolePermissionList()) {
      if (AclUserDetailsService.hasAnyAuthorities(rolePermissionData.getAuthorities())
          && rolePermissionData.hasPermission(permission)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks role preconditions for the current user.
   *
   * @param from
   * @param permission
   * @return
   */
  private boolean hasRoleCondition(From<?, ?> from, String permission) {
    return hasRoleCondition(aclMetaData.getAclEntityMetaData(from.getJavaType()), permission);
  }

  /**
   * Checks role preconditions for the current user.
   * 
   * @param javaType
   * @param permission
   * @return
   */
  private boolean hasRoleCondition(AclEntityMetaData metaData, String permission) {
    for (RolePermissionData roleConditionData : metaData.getRoleConditionList()) {
      if (AclUserDetailsService.hasAnyAuthorities(roleConditionData.getAuthorities())
          && roleConditionData.hasPermission(permission)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canBeCreated(Object newEntity) {
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(newEntity.getClass());
    if (metaData == null) {
      // Not handled entity type
      return true;
    }
    for (CreatePermissionData createPermissionData : metaData.getCreatePermissionList()) {
      if (AclUserDetailsService.hasAnyAuthorities(createPermissionData.getAuthorities())) {
        return true;
      }
    }
    return hasRolePermission(metaData, CREATE_PERMISSION);
  }

  /**
   * Creates a predicate for current user to its own entity
   *
   */
  private List<Predicate> createSelfPredicates(From<?, ?> from, CriteriaBuilder cb, Object userId, String permission) {
    List<Predicate> predicates = new ArrayList<>();
    if (AclUser.class.isAssignableFrom(from.getJavaType())) {
      if (aclMetaData.getSelfPermissions().hasPermission(permission)) {
        LOG.trace("Adding 'self' predicate for {}", from.getJavaType());
        SingularAttribute<? super Object, ?> idAttribute = aclMetaData.getAclEntityMetaData(from.getJavaType())
            .getIdAttribute();
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
  private List<Predicate> createOwnerPredicates(From<?, ?> from, CriteriaBuilder cb, Object userId, String permission,
      boolean ownerGroup) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (OwnerData ownerData : metaData.getOwnerDataList()) {
      if (ownerGroup || ownerData.hasPermission(permission)) {
        LOG.trace("Adding 'owner' predicate for {}.{}", from.getJavaType(), ownerData.getPropertyName());
        SingularAttribute<? super Object, ?> idAttribute = aclMetaData.getAclEntityMetaData(ownerData.getPropertyType())
            .getIdAttribute();
        if (ownerData.isCollection()) {
          predicates.add(cb.equal(from.join(ownerData.getPropertyName(), JoinType.LEFT).get(idAttribute), userId));
        } else {
          predicates.add(cb.equal(from.get(ownerData.getPropertyName()).get(idAttribute), userId));
        }
      }
    }
    return predicates;
  }

  // TODO refactor OwnerGroup Predicates to use prefixes. Or use ParentPredicates instead
  /**
   * Creates predicates for indirect owners defined by {@link AclOwner} annotation on NON-AclUser fields
   *
   * @param permission
   */
  private List<Predicate> createOwnerGroupPredicates(From<?, ?> from, CriteriaBuilder cb, Object userId,
      String permission) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (OwnerData ownerGroupData : metaData.getOwnerGroupDataList()) {
      if (ownerGroupData.hasPermission(permission)) {
        LOG.trace("Adding 'owner-group' predicate for {}.{}", from.getJavaType(), ownerGroupData.getPropertyName());
        predicates.addAll(createOwnerPredicates(from.join(ownerGroupData.getPropertyName(), JoinType.LEFT), cb, userId,
            permission, true));
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
  private List<Predicate> createParentPredicates(From<?, ?> from, CriteriaBuilder cb, Object userId, String permission,
      int depth) {
    List<Predicate> predicates = new ArrayList<>();
    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (ParentData parentData : metaData.getParentDataList()) {
      if (parentData.hasPermission(permission)) {
        LOG.trace("Adding 'parent' sub-predicates for {}.{}", from.getJavaType(), parentData.getPropertyName());
        String permissionPrefix = parentData.getPermissionPrefix();
        String parentPermission = permissionPrefix.isEmpty() ? permission
            : permissionPrefix + PERMISSION_PREFIX_DELIMITER + permission;
        // create predicates recursively on parent objects using prefixed permission
        predicates.add(toSubPredicate(from.join(parentData.getPropertyName(), JoinType.LEFT), cb, userId,
            parentPermission, depth - 1));
      }
    }
    return predicates;
  }

  /**
   * Creates predicates for permissionLinks
   *
   */
  private List<Predicate> createPermissionLinkPredicates(From<?, ?> from, CriteriaBuilder cb, Object userId,
      String permission) {
    List<Predicate> predicates = new ArrayList<>();

    AclEntityMetaData metaData = aclMetaData.getAclEntityMetaData(from.getJavaType());
    for (PermissionLinkData permissionLinkData : metaData.getPermissionLinkList()) {
      LOG.trace("Adding 'permission-link' predicate for {}.{}", from.getJavaType(),
          permissionLinkData.getPropertyName());
      Join<Object, Object> permissionLink = from.join(permissionLinkData.getPropertyName(), JoinType.LEFT);
      permissionLink
          .on(createOnPredicate(cb, permissionLink.<String> get(permissionLinkData.getPermissionField()), permission));
      predicates.addAll(createOwnerPredicates(permissionLink, cb, userId, permission, false));
      predicates.addAll(createOwnerGroupPredicates(permissionLink, cb, userId, permission));
    }

    return predicates;
  }

  private Predicate createOnPredicate(CriteriaBuilder cb, Expression<String> field, String permission) {
    int index = permission.lastIndexOf(PERMISSION_PREFIX_DELIMITER) + 1;

    List<Predicate> onPredicates = createOnPredicate(cb, field, permission.substring(index),
        permission.substring(0, index));
    return cb.or(onPredicates.toArray(new Predicate[onPredicates.size()]));
  }

  private List<Predicate> createOnPredicate(CriteriaBuilder cb, Expression<String> field, String permission,
      String prefixes) {
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
