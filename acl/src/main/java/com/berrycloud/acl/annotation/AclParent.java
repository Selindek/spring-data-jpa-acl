package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property is the parent of the current entity. That means if a user has permission on the parent object then
 * she also has permission on this object. Can be used only on AclEntity types.
 * 
 * @author Istvan Ratkai
 *
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Documented
public @interface AclParent {
	/**
	 * The permissions which are inherited from this parent
	 */
	String[] value() default {ALL_PERMISSION};

	/**
	 * The permission prefix for parent permissions. The user will gain permission to the current object if she has {prefix}permission to
	 * the parent object. Default is empty string.
	 */
	String prefix() default "";
}
