package com.berrycloud.acl;

import java.io.Serializable;
import java.util.Set;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public interface AclLogic {

	AclUser<Serializable, AclRole<Serializable>> loadUserByUsername(String username);

	Set<AclRole<Serializable>> getAllRoles(AclUser<Serializable, AclRole<Serializable>> aclUser);

	Serializable getUserId(AclUser<Serializable, AclRole<Serializable>> user);

}
