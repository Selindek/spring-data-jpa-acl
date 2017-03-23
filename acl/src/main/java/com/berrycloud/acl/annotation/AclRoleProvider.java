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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated property can provide roles for this AclUser. It can be used on properties of an AclUser,
 * otherwise ignored. All of the annotated properties are checked for AclRole and Collections of AclRole properties, and
 * those roles are added to the UserDeatails of the current user. Can be used on collections and simple properties.
 *
 * @author István Rátkai (Selindek)
 *
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Documented
public @interface AclRoleProvider {
}
