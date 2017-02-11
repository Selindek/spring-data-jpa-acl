package com.berrycloud.acl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property is the parent of the current entity.
 * That means if a user has permission on the parent object then she also has permission on this object.
 * Can be used only on AclEntity types.
 *  
 * @author Istvan Ratkai
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface AclParent {

}
