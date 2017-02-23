package com.berrycloud.acl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property can provide roles for this AclUser. It can be used on properties of an AclUser, otherwise ignored. All of the
 * annotated properties are checked for Collections of AclRole properties during login, and those roles are added to the UserDeatails of the current user.
 * Can be used on collections and simple properties.
 * 
 * @author Istvan Ratkai
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface AclRoleProvider {
}
