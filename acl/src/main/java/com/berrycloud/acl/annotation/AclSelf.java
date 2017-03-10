package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclConstants.ALL_PERMISSION;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines the permissions the users have on their own user-entity. Only classes implementing the AclUser interface can
 * be annotated by this annotation.
 *
 * @author Istvan Ratkai
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface AclSelf {
    /**
     * Defines the list of permissions this users have on their own user-entity.
     */
    String[] value() default { ALL_PERMISSION };

}
