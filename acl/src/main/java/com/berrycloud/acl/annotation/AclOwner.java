package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.berrycloud.acl.domain.PermissionLink;

/**
 * Indicates that the annotated property is the owner of this entity. It must be an AclUser otherwise it's ignored.
 * 
 * @author Istvan Ratkai
 *
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Documented
public @interface AclOwner {
	/**
	 * Defines the list of permissions this owner has on the current target.
	 */
	String[] value() default {ALL_PERMISSION};

	/**
	 * Defines the field in the annotated AclEntity what contains the permission. Using this parameter we can manually define
	 * permission-links. (By NOT extending the {@link PermissionLink} class) If this property is set then the value property is ignored. If
	 * the annotated AclEntity has no field defined with the given name then a warning will be logged and this annotation is ignored.
	 */

	String permissionField() default "";
}
