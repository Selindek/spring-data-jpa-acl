/*
 * Copyright 2012-2017 the original author or authors.
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

import org.springframework.data.rest.core.event.BeforeLinkDeleteEvent;
import org.springframework.data.rest.core.event.BeforeLinkSaveEvent;
import org.springframework.data.rest.core.event.LinkedEntityEvent;

/**
 * Container class for storing property name and value for {@link LinkedEntityEvent}s.
 * 
 * @author István Rátkai (Selindek)
 *
 */
public class PropertyReference {

  public PropertyReference(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  private String name;
  private Object value;

  /**
   * The name of the linked property which was changed.
   * 
   * @return the name of the property.
   */
  public String getName() {
    return name;
  }

  /**
   * <p>
   * The referenced object(s) which were changed. If this object is a parameter in a {@link BeforeLinkDeleteEvent} then
   * it contains the deleted object(s). If it's a parameter of a {@link BeforeLinkSaveEvent} then it contains the newly
   * added object(s).
   * </p>
   * 
   * <ul>
   * <li>If the property is a single-value property then this is either the deleted value or the new value which was
   * added.</li>
   * <li>If the property is a map then it contains either the removed/changed entries or the added/changed entries.</li>
   * <li>If the property is a collection then it contains either the removed elements or the added elements.</li>
   * </ul>
   * 
   * 
   * @return
   */
  public Object getValue() {
    return value;
  }
}
