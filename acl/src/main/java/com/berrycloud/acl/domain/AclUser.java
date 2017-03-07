package com.berrycloud.acl.domain;

import java.util.Set;

import javax.persistence.Column;

import com.berrycloud.acl.annotation.AclOwner;
import com.berrycloud.acl.annotation.AclParent;
import com.berrycloud.acl.annotation.AclRoleCondition;
import com.berrycloud.acl.annotation.AclRolePermission;
import com.berrycloud.acl.annotation.AclSelf;

/**
 * Interface for User domain class. There should be exactly one domain class in the context what implements this
 * interface. Users are handled automatically by the AclUserdetailsservice (or its extensions) and can gain permissions
 * to other entities via {@link AclRole} entities and Acl role annotations like {@link AclRolePermission} or
 * {@link AclRoleCondition}, Acl permission annotations like {@link AclOwner}, {@link AclParent} or {@link AclSelf}, or
 * via {@link permissionLink} entities.
 *
 * @author Istvan Ratkai (Selindek)
 *
 * @param <R>
 *            A domain class what implements the {@link AclRole} interface
 */
public interface AclUser<R extends AclRole> {

    Set<R> getAclRoles();

    @Column(nullable = false, unique = true)
    String getUsername();

    String getPassword();
}
