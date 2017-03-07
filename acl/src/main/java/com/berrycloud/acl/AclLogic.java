package com.berrycloud.acl;

import java.io.Serializable;
import java.util.Set;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;

public interface AclLogic {

    AclUser<AclRole> loadUserByUsername(String username);

    Set<AclRole> getAllRoles(AclUser<AclRole> aclUser);

    Serializable getUserId(AclUser<AclRole> user);

    boolean isManagedType(Class<?> javaType);
}
