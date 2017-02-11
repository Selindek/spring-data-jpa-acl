package com.berrycloud.acl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.annotation.AclRoleProvider;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclPermission;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.domain.PermissionLink;

public class AclLogicImpl implements AclLogic {

  private static Logger LOG = LoggerFactory.getLogger(AclLogicImpl.class);

  @PersistenceContext
  private EntityManager em;

  private Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType;
  private Class<AclRole<Serializable>> aclRoleType;

  @SuppressWarnings("unchecked")
  public AclMetaData createAclMetaData() {
    Set<Class<?>> javaTypes = createJavaTypeSet();

    aclUserType = (Class<AclUser<Serializable, AclRole<Serializable>>>) searchEntityType(javaTypes, AclUser.class);
    aclRoleType = (Class<AclRole<Serializable>>) searchEntityType(javaTypes, AclRole.class);
    Class<AclPermission<Serializable>> aclPermissionType = (Class<AclPermission<Serializable>>) searchEntityType(javaTypes, AclPermission.class);
    // TODO add default user if using SimpleAclUser

    Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> metaDataMap = createMetaDataMap(javaTypes);

    return new AclMetaData(aclUserType, aclRoleType, aclPermissionType, metaDataMap);
  }

  private Set<Class<?>> createJavaTypeSet() {
    Set<Class<?>> javaTypes = new HashSet<>();
    for (EntityType<?> et : em.getMetamodel().getEntities()) {
      Class<?> type = et.getJavaType();
      if (!Modifier.isAbstract(type.getModifiers())) {
        javaTypes.add(type);
      }
    }
    return javaTypes;
  }

  private Class<?> searchEntityType(Set<Class<?>> javaTypes, Class<?> checkType) {
    Class<?> foundType = null;
    for (Class<?> type : javaTypes) {
      if (checkType.isAssignableFrom(type)) {
        if (foundType != null) {
          throw new IllegalStateException(
              "Multiple managed entity of class " + checkType.getSimpleName() + " found: " + foundType.getName() + " and " + type.getName());
        }
        foundType = type;
        LOG.debug(checkType.getSimpleName() + " found: " + foundType.getName());
      }
    }
    return foundType;
  }

  @SuppressWarnings("unchecked")
  private Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> createMetaDataMap(Set<Class<?>> javaTypes) {
    Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> metaDataMap = new HashMap<>();
    for (Class<?> javaType : javaTypes) {
      // Collect MetaData for AclEntities only
      if (AclEntity.class.isAssignableFrom(javaType)) {
        LOG.debug("Create metadata for {}", javaType);
        metaDataMap.put((Class<? extends AclEntity<Serializable>>) javaType, createAclEntityMetaData(javaType));
      }
    }
    return metaDataMap;
  }

  private AclEntityMetaData createAclEntityMetaData(Class<?> javaType) {
    AclEntityMetaData metaData = new AclEntityMetaData();
    try {
      // We use BeanWrapper for checking annotations on fields AND getters and setters too
      BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(javaType.newInstance());
      for (final PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
        final String propertyName = propertyDescriptor.getName();
        final TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor(propertyName);
        checkAclOwner(metaData, javaType, propertyName, typeDescriptor);
        checkAclParent(metaData, javaType, propertyName, typeDescriptor);
      }
    } catch (InstantiationException | IllegalAccessException e) {
      LOG.error("Cannot instantiate {} ", javaType);
    }

    checkAclPermissionsLinks(metaData, javaType);

    return metaData;
  }

  private void checkAclPermissionsLinks(AclEntityMetaData metaData, Class<?> javaType) {
    LOG.trace("Collect permissionlinks for {}", javaType);
    for (Class<?> type : createJavaTypeSet()) {
      if (PermissionLink.class.isAssignableFrom(type)) {
        // Check generic parameter 'target' of PermissionLink class
        if (javaType.equals(ResolvableType.forClass(type).as(PermissionLink.class).getGeneric(1).getRawClass())) {
          metaData.getOwnerPermissionList().add(type);
        }
        // Check generic parameter 'owner' of PermissionLink class
        if (javaType.equals(ResolvableType.forClass(type).as(PermissionLink.class).getGeneric(0).getRawClass())) {
          metaData.getTargetPermissionList().add(type);
        }
      }
    }
  }

  private void checkAclOwner(AclEntityMetaData metaData, Class<?> javaType, final String propertyName, final TypeDescriptor typeDescriptor) {
    final AclOwner aclOwner = typeDescriptor.getAnnotation(AclOwner.class);
    if (aclOwner != null) {
      if (AclUser.class.isAssignableFrom(typeDescriptor.getObjectType())) {
        metaData.getOwnerDataList().add(new OwnerData(propertyName, Arrays.asList(aclOwner.value())));
      } else {
        LOG.warn("Non-AclUser property '{}' in {} is annotated by @AclOwner ... ignored", propertyName, javaType);
      }
    }
  }

  private void checkAclParent(AclEntityMetaData metaData, Class<?> javaType, final String propertyName, final TypeDescriptor typeDescriptor) {
    final AclParent aclParent = typeDescriptor.getAnnotation(AclParent.class);
    if (aclParent != null) {
      if (AclEntity.class.isAssignableFrom(typeDescriptor.getObjectType())) {
        metaData.getParentList().add(propertyName);
      } else {
        LOG.warn("Non-AclEntity property '{}' in {} is annotated by @AclParent ... ignored", propertyName, javaType);
      }
    }
  }

  @Override
  public Set<AclRole<Serializable>> getAllRoles(AclUser<Serializable, AclRole<Serializable>> aclUser) {

    Set<AclRole<Serializable>> roleSet = new HashSet<>();

    roleSet.addAll(getRoles(aclUser));

    // TODO collect all roles from attached properties
    BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(aclUser);
    for (final PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
      final String propertyName = propertyDescriptor.getName();
      final TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor(propertyName);
      AclRoleProvider roleProvider = typeDescriptor.getAnnotation(AclRoleProvider.class);
      if (roleProvider != null) {
        LOG.trace("Found AclRoleProvider: {}.{}", aclUser.getClass(), propertyName);
        @SuppressWarnings("unchecked")
        Collection<Object> values = beanWrapper.convertIfNecessary(beanWrapper.getPropertyValue(propertyName), Collection.class);
        for (Object value : values) {
          LOG.trace("Collecting roles from {}", value);
          roleSet.addAll(getRoles(value));
        }
      }
    }

    return roleSet;
  }

  private Collection<AclRole<Serializable>> getRoles(Object entity) {
    Collection<AclRole<Serializable>> roleSet = new ArrayList<>();
    BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
    for (final PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
      final String propertyName = propertyDescriptor.getName();
      final TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor(propertyName);
      final TypeDescriptor elementTypeDescriptor = typeDescriptor.getElementTypeDescriptor();
      if (aclRoleType == typeDescriptor.getType() || (elementTypeDescriptor != null && aclRoleType == elementTypeDescriptor.getType())) {
        @SuppressWarnings("unchecked")
        Collection<AclRole<Serializable>> roles = beanWrapper.convertIfNecessary(beanWrapper.getPropertyValue(propertyName), Collection.class);
        if (roles != null) {
          LOG.trace("Add roles from {}.{} : {}", entity.getClass(), propertyName, roles);
          roleSet.addAll(roles);
        }
      }
    }

    return roleSet;
  }

  /**
   * Load AclUser by username without any permission checking. This method is for internal use only
   */
  @Override
  @Transactional(readOnly = true)
  public AclUser<Serializable, AclRole<Serializable>> loadUserByUsername(String username) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AclUser<Serializable, AclRole<Serializable>>> query = cb.createQuery(aclUserType);
    Root<AclUser<Serializable, AclRole<Serializable>>> root = query.from(aclUserType);
    query.select(root).where(cb.equal(root.get("username"), username));

    AclUser<Serializable, AclRole<Serializable>> aclUser = em.createQuery(query).getSingleResult();

    if (aclUser == null) {
      throw (new UsernameNotFoundException("User with username'" + username + "' cannot be found."));
    }
    return aclUser;
  }

}
