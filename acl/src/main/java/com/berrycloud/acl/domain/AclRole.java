package com.berrycloud.acl.domain;

import javax.persistence.Column;

/**
 * Interface for role Entity. (Role is basically a string what becomes the 'authority' property of a GrantedAuthority
 * object what will be assigned to the associated Users.) There should be exactly one domain class in the context what
 * implements this interface. If there is no such a class in the context the {@link SimpleAclRole} class is used as
 * default implementation.
 * <p>
 * {@code AclRole} entities will be automatically picked up when a user object is loaded by the AclUserDetailsService.
 *
 * @author Istvan Ratkai (Selindek)
 *
 */
public interface AclRole {

    @Column(nullable = false, unique = true)
    String getRoleName();

}
