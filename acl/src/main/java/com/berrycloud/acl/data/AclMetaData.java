package com.berrycloud.acl.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public class AclMetaData {
	private Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType;
	private Class<AclRole<Serializable>> aclRoleType;
	private PermissionData selfPermissions;

	private Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> metaDataMap;

	public AclMetaData(Class<AclUser<Serializable, AclRole<Serializable>>> aclUserType, Class<AclRole<Serializable>> aclRoleType,
			Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> metaDataMap, PermissionData selfPermissions) {
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

	public Class<AclUser<Serializable, AclRole<Serializable>>> getAclUserType() {
		return aclUserType;
	}

	public Class<AclRole<Serializable>> getAclRoleType() {
		return aclRoleType;
	}

	public Map<Class<? extends AclEntity<Serializable>>, AclEntityMetaData> getMetaDataMap() {
		return metaDataMap;
	}

	public AclEntityMetaData getAclEntityMetaData(Class<?> entityClass) {
		return metaDataMap.get(entityClass);
	}

	public AclEntityMetaData getAclEntityMetaData(AclEntity<Serializable> entity) {
		return metaDataMap.get(entity.getClass());
	}

}
