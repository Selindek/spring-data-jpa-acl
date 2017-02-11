package com.berrycloud.acl.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property is the owner of this entity.
 * It must be an AclUser otherwise it's ignored.
 *  
 * @author Istvan Ratkai
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface AclOwner {
  String[] value() default { "read", "update", "delete" };
}
