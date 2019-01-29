/*
 * Copyright 2014-2018 the original author or authors.
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
package org.springframework.data.rest.webmvc;

import org.springframework.data.jpa.repository.support.AclReadPermissionException;
import org.springframework.data.jpa.repository.support.InsufficientAclPermissionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handler for ACl specific exceptions.
 *
 * @author István Rátkai (Selindek)
 *
 */
@ControllerAdvice
public class RepositoryAclRestExceptionHandler {

  /**
   * Handles {@link AclReadPermissionException} by returning {@code 404 Not Found}.
   *
   * @param o_O
   *          the exception to handle.
   * @return
   */
  @ExceptionHandler
  ResponseEntity<?> handleNotFound(AclReadPermissionException o_O) {
    return new ResponseEntity<>(new HttpHeaders(), HttpStatus.NOT_FOUND);
  }

  /**
   * Handles {@link InsufficientAclPermissionException} by returning {@code 403 Forbidden}.
   *
   * @param o_O
   *          the exception to handle.
   * @return
   */
  @ExceptionHandler
  ResponseEntity<?> handleForbidden(InsufficientAclPermissionException o_O) {
    return new ResponseEntity<>(new HttpHeaders(), HttpStatus.FORBIDDEN);
  }

}
