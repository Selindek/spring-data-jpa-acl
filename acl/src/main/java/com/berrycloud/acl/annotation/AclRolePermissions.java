package com.berrycloud.acl.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container annotation for RolePermission annotations
 *
 * @author Istvan Ratkai (Selindek)
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface AclRolePermissions {

    /**
     * The list of permissions the roles defined in the role field gain.
     */
    AclRolePermission[] value();

}
