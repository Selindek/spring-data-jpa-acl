/*
 * Copyright 2008-2017 the original author or authors.
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
package org.springframework.data.jpa.repository.support;

/**
 * <p>
 * Thrown by the AclRepository implementation when an entity cannot be deleted.
 * </p>
 * It could mean that the entity not exist at all OR it exists but the current authentication has no DELETE permission
 * to it.
 */
public class AclDeletePermissionException extends InsufficientAclPermissionException {

  private static final long serialVersionUID = -1223599023026071817L;

  /**
   * Constructs a new <code>AclDeletePermissionException</code> exception with <code>null</code> as its detail message.
   */
  public AclDeletePermissionException() {
    super();
  }

  /**
   * Constructs a new <code>AclDeletePermissionException</code> exception with the specified detail message.
   * 
   * @param message
   *          the detail message.
   */
  public AclDeletePermissionException(String message) {
    super(message);
  }

}
