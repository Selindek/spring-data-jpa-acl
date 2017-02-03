package com.berrycloud.acl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.data.OwnerData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclPermission;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;

public class AclLogicImpl implements AclLogic {

  private static Logger LOG = LoggerFactory.getLogger(AclLogicImpl.class);

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private AclUserDetailsService<? extends GrantedAuthority> aclUserDetailsService;

  @SuppressWarnings("unchecked")
  public AclMetaData createAclMetaData() {
    Set<EntityType<?>> entities = em.getMetamodel().getEntities();
    Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType = (Class<AclUser<Serializable, AclRole<Serializable>>>) searchEntityType(entities,
        AclUser.class);
    Class<AclRole<Serializable>> aclRoleType = (Class<AclRole<Serializable>>) searchEntityType(entities, AclRole.class);
    Class<AclPermission<Serializable>> aclPermissionType = (Class<AclPermission<Serializable>>) searchEntityType(entities, AclPermission.class);
    // TODO add default user if using SimpleAclUser

    Map<Class<? extends AclEntity>, AclEntityMetaData> metaDataMap = createMetaDataMap(entities);

    return new AclMetaData(aclUserType, aclRoleType, aclPermissionType, metaDataMap);
  }

  private Class<?> searchEntityType(Set<EntityType<?>> entities, Class<?> checkType) {
    Class<?> foundType = null;
    for (EntityType<?> et : entities) {
      Class<?> type = et.getJavaType();
      if (!Modifier.isAbstract(type.getModifiers()) && checkType.isAssignableFrom(type)) {
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
  private Map<Class<? extends AclEntity>, AclEntityMetaData> createMetaDataMap(Set<EntityType<?>> entities) {
    Map<Class<? extends AclEntity>, AclEntityMetaData> metaDataMap = new HashMap<>();
    for (EntityType<?> et : entities) {
      Class<?> javaType = et.getJavaType();
      // Collect MetaData for AclEntities only
      if (!Modifier.isAbstract(javaType.getModifiers()) && AclEntity.class.isAssignableFrom(javaType)) {
        LOG.info("Create metadata for {}", javaType);
        metaDataMap.put((Class<? extends AclEntity>) javaType, createAclEntityMetaData(et));
      }
    }
    return metaDataMap;
  }

  private AclEntityMetaData createAclEntityMetaData(EntityType<?> et) {
    AclEntityMetaData metaData = new AclEntityMetaData();
    Class<?> javaType = et.getJavaType();
    try {
      // We use BeanWrapper for checking annotations on fields AND getters and setters too
      BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(javaType.newInstance());

      for (final PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
        final String propertyName = propertyDescriptor.getName();
        final TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor(propertyName);
        checkAclOwner(metaData, javaType, propertyName, typeDescriptor);
      }
    } catch (InstantiationException | IllegalAccessException e) {
      LOG.error("Cannot instantiate {} ", javaType);
    }
    return metaData;
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

  @Override
  public AclUserDetails getCurrentUser() {
    return (AclUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  @Override
  public boolean isAdmin() {
    // TODO create constants for common authorities
    return hasAuthority("ROLE_ADMIN");
  }

  @Override
  public boolean hasAuthority(String authority) {
    return getCurrentUser().getAuthorities().contains(aclUserDetailsService.createGrantedAuthority(authority));
  }

}
