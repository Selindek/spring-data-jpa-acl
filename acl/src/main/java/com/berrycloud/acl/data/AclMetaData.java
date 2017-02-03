package com.berrycloud.acl.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclPermission;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class AclMetaData {
  private Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType;
  private Class<AclRole<Serializable>> aclRoleType;
  private Class<AclPermission<Serializable>> aclPermissionType;
  
  private Map<Class<AclEntity>, AclEntityMetaData> metaDataMap;

  public AclMetaData(Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType, Class<AclRole<Serializable>> aclRoleType,
      Class<AclPermission<Serializable>> aclPermissionType, Map<Class<AclEntity>, AclEntityMetaData> metaDataMap) {
    super();
    this.aclUserType = aclUserType;
    this.aclRoleType = aclRoleType;
    this.aclPermissionType = aclPermissionType;
    this.metaDataMap = Collections.unmodifiableMap(metaDataMap);
  }

  public Class<AclUser<Serializable, AclRole<Serializable>>> getAclUserType() {
    return aclUserType;
  }

  public Class<AclRole<Serializable>> getAclRoleType() {
    return aclRoleType;
  }

  public Class<AclPermission<Serializable>> getAclPermissionType() {
    return aclPermissionType;
  }

  public Map<Class<AclEntity>, AclEntityMetaData> getMetaDataMap() {
    return metaDataMap;
  }
  
  public AclEntityMetaData getAclEntityMetaData(Class<AclEntity> entityClass) {
    return metaDataMap.get(entityClass);
  }

  public AclEntityMetaData getAclEntityMetaData(AclEntity entity) {
    return metaDataMap.get(entity.getClass());
  }

}
