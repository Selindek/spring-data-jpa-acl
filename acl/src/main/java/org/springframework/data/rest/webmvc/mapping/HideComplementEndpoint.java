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
package org.springframework.data.rest.webmvc.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signing annotation for hiding the complement endpoints for collection-like properties. This annotation can be used on
 * an entity for hiding the complement-collections for all of its collections or on a particular property for hiding
 * only the complement-endpoint for that property.
 * <p>
 * Complement-endpoints are NOT invalid even if they are hidden. (But for certain collections the complement-collection
 * is meaningless or useless, so rendering the hateoas link for it is unnecessary.)
 * 
 * @author István Rátkai (Selindek)
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HideComplementEndpoint {

}
