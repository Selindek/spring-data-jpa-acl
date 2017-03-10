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

/**
 * Indicates that the annotated property is the parent of the current entity. That means if a user has permission on the
 * parent object then she also has permission on this object. Can be used only on Entities.
 *
 * <p>
 * You can define multiple permissions in the {@link #value} field. If the list contains {@code "all"} then it provides
 * all possible permissions. If the list contains any permissions (not empty) it automatically provides {@code "read"}
 * permission too.
 * <p>
 * See detailed examples under the {@link #prefix} field.
 * <p>
 * You can build a complex permission tree using the {@link AclParent} annotation on several fields in several domain
 * classes. Using too many {@link AclParent} annotation however could make the acl-queries extremely complex and could
 * slow down your repository methods. Try to design your permission hierarchy as simple as possible.
 * <p>
 * Also there is a chance that you accidentally create a parent-loop in your permission hierarchy. To avoid infinite
 * loops you can use the following setting in your application.properties file:
 *
 * <pre>
 * spring.data.jpa.acl.max-depth:2
 * </pre>
 *
 * @author István Rátkai (Selindek)
 *
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface AclParent {
    /**
     * The permissions which are inherited from this parent
     */
    String[] value() default { ALL_PERMISSION };

    /**
     * The permission prefix for parent permissions. The user will gain permission to the current object if she has
     * {prefix}permission to the parent object. Default is empty string.
     * <p>
     * The prefix cannot contain the prefix-delimiter character. '-' by default.
     *
     * <pre>
     * &#64;Entity
     * public class Document {
     * ...
     *
     * &#64;Owner
     * &#64;Parent({"read","update"})
     * &#64;ManyToOne
     * private User author;
     *
     * ...
     * }
     * </pre>
     *
     * In the example above there are two annotations on the author field. The {@link AclOwner} annotation indicates
     * that the User in the author field is the owner of this entity and it has all of the possible permissions to this
     * entity. (read/update/delete or any other user-defined permissions. ).
     * <p>
     * The {@link AclParent} annotation however indicates that the User object in the author field is also the parent of
     * this entity, so {@code "read"} and {@code "update"} permissions are inherited from it. It means that if somebody
     * has {@code "read"} or {@code "update"} permission to the author of this entity then the same user automatically
     * gains those permissions to this entity. So if we have the following settings in the User domain class:
     *
     * <pre>
     * &#64;Entity
     * public class User extends AclUser {
     * ...
     *
     * &#64;AclOwner
     * &#64;ManyToOne
     * private User supervisor;
     *
     * &#64;OneToMany(mappedBy = "author")
     * private List<Document> documents;
     *
     * ...
     * }
     * </pre>
     *
     * ... then the supervisor of the user can read and modify all of the documents of the user. (And he can also read,
     * modify or even delete the user object itself)
     * <p>
     * Let's see a more complex example where the {@link #prefix} field is not empty:
     *
     * <pre>
     *
     * &#64;Entity
     * public class Document {
     * ...
     *
     * &#64;Parent(prefix="documents", value="all")
     * &#64;Owner
     * &#64;ManyToOne
     * private User author;
     *
     * ...
     * }
     *
     * &#64;Entity
     * public class User extends AclUser {
     * ...
     *
     * &#64;AclOwner({"all","document-read"})
     * &#64;ManyToOne
     * private User supervisor;
     *
     * &#64;AclOwner({"read","document-update"})
     * &#64;ManyToMany
     * private List&lt;User&gt; editors;
     *
     * &#64;OneToMany(mappedBy = "author")
     * private List&lt;Document&gt; documents;
     *
     * ...
     * }
     *
     * </pre>
     *
     * In this example the author of the document still has the same ({@code "all"}) permissions. However the document
     * objects now inherit permissions from their author object only if they have the {@code documents} prefix.
     * <p>
     * The supervisor of the user still has {@code "all"} permission, but this time it affects only the User object
     * itself. Only the permissions started with the prefix {@code documents} are inherited, so the supervisor still can
     * update or even delete the User object, but he can only read the supervised users' documents and cannot modify
     * them, because she has only {@code document-read} permission. However the editors of a user have
     * {@code document-update} permission, so they can edit the documents, but they cannot modify the user entity,
     * because they have only {@code read} permission to the user object itself.
     * <p>
     * You can also create multi-level parent- and prefix-hierarchies. Let's assume we can add attachments to our
     * documents:
     *
     * <pre>
     * &#64;Entity
     * public class Attachment {
     * ...
     *
     * &#64;AclParent(prefix="attachments")
     * &#64;ManyToOne
     * private Document document
     *
     * ...
     * }
     * </pre>
     *
     * The parent of the attachment objects is a document, the parent of the document is a user. Because we have
     * {@code attachments} prefix in the {@code Attachment} domain class and {@code documents} prefix in the
     * {@code Document} class only users with {@code documents-attachments-read} permission to a user can access the
     * attachments of the user's documents.
     * <p>
     * If the prefix field was not set in the example above, then the attachments can be accessed with
     * {@code documents-read} permission, because the attachments inherit their parents' permissions without any
     * additional prefix.
     */
    String prefix() default "";
}
