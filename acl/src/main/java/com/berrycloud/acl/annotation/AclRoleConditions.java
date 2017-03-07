package com.berrycloud.acl.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container annotation for {@link AclRoleCondition} annotations
 *
 * @author Istvan Ratkai (Selindek)
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface AclRoleConditions {

    /**
     * The list of {@link AclRoleCondition} annotations
     */
    AclRoleCondition[] value();

}
