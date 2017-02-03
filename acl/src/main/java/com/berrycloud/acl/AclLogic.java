package com.berrycloud.acl;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.berrycloud.acl.data.AclEntityMetaData;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclPermission;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class AclLogic {

  private static Logger LOG = LoggerFactory.getLogger(AclLogic.class);

  @Autowired
  private EntityManager em;

  @SuppressWarnings("unchecked")
//  @PostConstruct
  public AclMetaData createAclMetaData() {
    Set<EntityType<?>> entities = em.getMetamodel().getEntities();
    Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType = (Class<AclUser<Serializable, AclRole<Serializable>>>) searchEntityType(entities, AclUser.class);
    Class<AclRole<Serializable>> aclRoleType = (Class<AclRole<Serializable>>) searchEntityType(entities, AclRole.class);
    Class<AclPermission<Serializable>> aclPermissionType = (Class<AclPermission<Serializable>>) searchEntityType(entities, AclPermission.class);
    // TODO add default user if using SimpleAclUser

    Map<Class<AclEntity>, AclEntityMetaData> metaDataMap = createMetaDataMap(entities);
    
    return new AclMetaData(aclUserType, aclRoleType, aclPermissionType, metaDataMap);
  }

  private Class<?> searchEntityType(Set<EntityType<?>> entities, Class<?> checkType) {
    Class<?> foundType = null;
    for (EntityType<?> et : entities) {
      Class<?> type = et.getJavaType();
      if (!Modifier.isAbstract(type.getModifiers()) && checkType.isAssignableFrom(type)) {
        if (foundType != null) {
          throw new IllegalStateException("Multiple managed entity of class " + checkType.getSimpleName() + " found: " + foundType.getName() + " and " + type.getName());
        }
        foundType = type;
        LOG.debug(checkType.getSimpleName() + " found: " + foundType.getName());
      }
    }
    return foundType;
  }

  
  @SuppressWarnings("unchecked")
  private Map<Class<AclEntity>, AclEntityMetaData> createMetaDataMap(Set<EntityType<?>> entities) {
    Map<Class<AclEntity>, AclEntityMetaData> metaDataMap = new HashMap<>();
    for (EntityType<?> et : entities) {
      Class<?> javaType =et.getJavaType();
      if(AclEntity.class.isAssignableFrom(javaType)) {
        metaDataMap.put((Class<AclEntity>) javaType, createAclEntityMetaData(et));
      }
    }
    return metaDataMap;
  }

  private AclEntityMetaData createAclEntityMetaData(EntityType<?> et) {
    AclEntityMetaData metaData = new AclEntityMetaData();
    return metaData;
  }


 }
