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
 * Thrown by the AclRepository implementation when an entity cannot be created.
 * </p>
 * It means that the current authentication has no CREATE permission to the given entity-type.
 */
public class AclCreatePermissionException extends InsufficientAclPermissionException {

  private static final long serialVersionUID = 2966170432939324204L;

  /**
   * Constructs a new <code>AclCreatePermissionException</code> exception with <code>null</code> as its detail message.
   */
  public AclCreatePermissionException() {
    super();
  }

  /**
   * Constructs a new <code>AclCreatePermissionException</code> exception with the specified detail message.
   * 
   * @param message
   *          the detail message.
   */
  public AclCreatePermissionException(String message) {
    super(message);
  }

}
