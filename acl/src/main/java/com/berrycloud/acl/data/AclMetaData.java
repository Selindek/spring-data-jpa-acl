package com.berrycloud.acl.data;

import java.util.Collections;
import java.util.Map;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

/**
 * A storage class containing all ACL metadata for all the managed entities and for the logic itself. The data is
 * constructed during startup and its used during permission-evaluation.
 */
public class AclMetaData {
    private Class<AclUser<AclRole>> aclUserType;
    private Class<AclRole> aclRoleType;
    private PermissionData selfPermissions;

    private Map<Class<?>, AclEntityMetaData> metaDataMap;

    public AclMetaData(Class<AclUser<AclRole>> aclUserType, Class<AclRole> aclRoleType,
            Map<Class<?>, AclEntityMetaData> metaDataMap, PermissionData selfPermissions) {
        super();
        this.aclUserType = aclUserType;
        this.aclRoleType = aclRoleType;
        this.metaDataMap = Collections.unmodifiableMap(metaDataMap);
        this.selfPermissions = selfPermissions;
    }

    public PermissionData getSelfPermissions() {
        return selfPermissions;
    }

    public void setSelfPermissions(PermissionData selfPermissions) {
        this.selfPermissions = selfPermissions;
    }

    public Class<AclUser<AclRole>> getAclUserType() {
        return aclUserType;
    }

    public Class<AclRole> getAclRoleType() {
        return aclRoleType;
    }

    public Map<Class<?>, AclEntityMetaData> getMetaDataMap() {
        return metaDataMap;
    }

    public AclEntityMetaData getAclEntityMetaData(Class<?> entityClass) {
        return metaDataMap.get(entityClass);
    }

    public AclEntityMetaData getAclEntityMetaData(Object entity) {
        return metaDataMap.get(entity.getClass());
    }

}
