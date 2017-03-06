package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a list of permissions for a list of roles for this domain object. All of the users who has at least one role
 * form the provided list will gain all the permissions defined here for all of the entities of this domain-object.
 *
 * If an AclEntity is NOT annotated with this annotation then the acl-logic treats as if it was annotated
 * as @AclRolePermission(role={"ROLE_ADMIN"}, value="all") So user with ADMIN_ROLE will gain all permissions to all
 * objects. However if you add this annotation to a domainObject, you have to manually set the roles for the
 * administrators. (Basically if you use this annotation you override the default behaviour)
 *
 *
 * @author Istvan Ratkai (Selindek)
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
@Repeatable(value = AclRolePermissions.class)
public @interface AclRolePermission {

    /**
     * The list of permissions the roles defined in the role field gain.
     */
    String[] value() default { ALL_PERMISSION };

    /**
     * The list of roles (by string representation) which gains the permission defined in the values field.
     */
    String[] role();
}
