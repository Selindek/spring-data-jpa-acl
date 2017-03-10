/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclConstants.ALL_PERMISSION;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.domain.PermissionLink;

/**
 * Indicates that the annotated property is the owner of this entity, i.e. it gains the given permissions to the current
 * entity.
 * <p>
 * If the annotated field (or getter method) is a {@link AclUser} entity or a collection of {@link AclUser} then that
 * user or those users gain the provided permissions.
 * <p>
 * If the annotated field is an entity (or entity collection) then all fields of the linked entity(s) which are either
 * {@link AclUser} or collection of {@link AclUser} AND annotated with {@link AclOwner} will gain the provided
 * permissions.
 * <p>
 * If the annotated field is not an entity then this annotation is ignored.
 *
 * <pre>
 * &#64;Entity
 * public class User extends AclUser {
 * ...
 *
 * &#64;AclOwner("all")
 * private User supervisor;
 *
 * &#64;AclOwner("read")
 * private List<WorkGroup> groups;
 *
 * ...
 * }
 * </pre>
 *
 * In the previous example there are two {@link AclOwner} annotations. The first one provides all permissions to the
 * supervisor of the user. (The supervisor can read or update the properties of this user or even delete the user).
 * <p>
 * The second annotation gives read permission for all of the members of all of the work-groups to this user entity
 * where this user is a member, assuming we have something like this in the {@code WorkGroup.java}:
 *
 * <pre>
 *
 * &#64;Entity
 * public class WorkGroup {
 * ...
 *
 * &#64;AclOwner("read")
 * private List<User> members;
 *
 * ...
 * }
 * </pre>
 * <p>
 * You can define multiple permissions in the {@link #value} field. If the list contains {@code "all"} then it provides
 * all possible permissions. If the list contains any permissions (not empty) it automatically provides {@code "read"}
 * permission too.
 *
 * @author István Rátkai (Selindek)
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface AclOwner {
    /**
     * Defines the list of permissions this owner will gain to the target entity.
     */
    String[] value() default { ALL_PERMISSION };

    /**
     * Defines the field in the annotated AclEntity what contains the provided permission. Using this parameter we can
     * manually define permission-links. (By NOT extending the {@link PermissionLink} class) If this property is set
     * then the value property is ignored. If the annotated AclEntity has no field defined with the given name then a
     * warning will be logged and this annotation is ignored.
     */

    String permissionField() default "";
}
