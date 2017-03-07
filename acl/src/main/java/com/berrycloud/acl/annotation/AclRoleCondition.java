package com.berrycloud.acl.annotation;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a list of roles for a list of permissions for this domain object as preconditions. In order to gain any of
 * the listed permissions a user must have at least one role amongst the given ones. Unlike the {@link AclRolePermission
 * } annotation this annotation doesn't automatically provide permission to the objects. Having a given role is only a
 * precondition for having the listed permissions. The user must have both the role AND a permission (via any
 * permission-role annotations like {@link AclOwner} or {@link AclParent} or a permission-link). A domain class can be
 * annotated multiple {@link AclRoleCondition} annotation.
 * <p>
 * An empty list in the {@code role} field means: ANY role.
 * <p>
 * Example: <blockquote>
 *
 * <pre>
 *     &#64;AclRoleCondition( role={}, value={"read"})
 *     &#64;AclRoleCondition( role={"ROLE_EDITOR"}, value={"update"})
 *     &#64;Entity
 *     public class Document {
 *     ...
 * </pre>
 *
 * </blockquote>
 * <p>
 * Any user could gain {@code read} permission to a {@code Document} object, but only users with {@code ROLE_EDITOR}
 * authority could gain {@code update } permission.
 * <p>
 * If this annotation is not used for a domain class the default behaviour is
 *
 * <pre>
 * &#64;AclRoleCondition( role={}, value={"all"})
 * </pre>
 *
 * i.e. any user <i>could</i> gain any permission to these entities.
 * <p>
 * You can define multiple permissions in the {@link #value} field. If the list contains {@code "all"} then it provides
 * all possible permissions. If the list contains any permissions (not empty) it automatically provides {@code "read"}
 * permission too.
 * <p>
 * You can also combine {@link AclRolePermission} and {@link AclRoleCondition} annotations for complex acl rules:
 *
 * <pre>
 *&#64;AclRolePermission( role={"ROLE_ADMIN"}, value={"read"})
 *&#64;AclRoleCondition( role={}, value={"update"})
 * </pre>
 *
 * The {@link AclRolePermission} annotations are checked first, so users with {@code ROLE_ADMIN} authority will
 * gain @{code "read"} permission automatically.
 * <p>
 * Any users could gain {@code "update"} or {@code "read"} permission via permission annotations. (Any elements in the
 * {link #value} field automatically grants {@code "read"} permission too).
 * <p>
 * However no one could gain {@code "delete"} or any other specific permissions to the annotated entities, because there
 * is no matching preconditions for any other permission type.
 * <p>
 * So if you want to completely deny the access for a domain class you can use the following annotation on it:
 * 
 * <pre>
 * &#64;AclRoleCondition( role={}, value={})
 * </pre>
 *
 * @author Istvan Ratkai (Selindek)
 *
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
@Repeatable(value = AclRoleConditions.class)
public @interface AclRoleCondition {

    /**
     * The list of permissions the roles defined in the role field gain.
     */
    String[] value() default { ALL_PERMISSION };

    /**
     * The list of roles (by string representation) which gains the permission defined in the values field. Empty list
     * means: ANY role. (No role preconditions for the given permissions)
     */
    String[] role();
}
