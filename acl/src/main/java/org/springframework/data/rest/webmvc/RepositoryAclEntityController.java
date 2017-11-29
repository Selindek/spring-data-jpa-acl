/*
 * Copyright 2013-2016 the original author or authors.
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

import java.io.Serializable;

import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.support.BackendId;
import org.springframework.data.rest.webmvc.support.DefaultedPageable;
import org.springframework.data.rest.webmvc.support.ETag;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RepositoryRestController
public class RepositoryAclEntityController extends RepositoryEntityController {

    private static final String BASE_MAPPING = "/{repository}";

    private static final String ACCEPT_HEADER = "Accept";

    public RepositoryAclEntityController(Repositories repositories, RepositoryRestConfiguration config,
            RepositoryEntityLinks entityLinks, PagedResourcesAssembler<Object> assembler,
            HttpHeadersPreparer headersPreparer) {
        super(repositories, config, entityLinks, assembler, headersPreparer);
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsForCollectionResource(RootResourceInformation information) {
        return super.optionsForCollectionResource(information);
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.HEAD)
    public ResponseEntity<?> headCollectionResource(RootResourceInformation resourceInformation,
            DefaultedPageable pageable) throws HttpRequestMethodNotSupportedException {
        return super.headCollectionResource(resourceInformation, pageable);
    }

    /** {@inheritDoc} */
    @Override
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.GET)
    public Resources<?> getCollectionResource(@QuerydslPredicate RootResourceInformation resourceInformation,
            DefaultedPageable pageable, Sort sort, PersistentEntityResourceAssembler assembler)
            throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
        return super.getCollectionResource(resourceInformation, pageable, sort, assembler);
    }

    /** {@inheritDoc} */
    @Override
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.GET, produces = {
            "application/x-spring-data-compact+json", "text/uri-list" })
    public Resources<?> getCollectionResourceCompact(@QuerydslPredicate RootResourceInformation resourceinformation,
            DefaultedPageable pageable, Sort sort, PersistentEntityResourceAssembler assembler)
            throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
        return super.getCollectionResourceCompact(resourceinformation, pageable, sort, assembler);
    }

    /** {@inheritDoc} */
    @Override
    @ResponseBody
    @RequestMapping(value = BASE_MAPPING, method = RequestMethod.POST)
    public ResponseEntity<ResourceSupport> postCollectionResource(RootResourceInformation resourceInformation,
            PersistentEntityResource payload, PersistentEntityResourceAssembler assembler,
            @RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
            throws HttpRequestMethodNotSupportedException {

        try {
            return super.postCollectionResource(resourceInformation, payload, assembler, acceptHeader);
        } catch (JpaObjectRetrievalFailureException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsForItemResource(RootResourceInformation information) {
        return super.optionsForItemResource(information);
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<?> headForItemResource(RootResourceInformation resourceInformation,
            @BackendId Serializable id, PersistentEntityResourceAssembler assembler)
            throws HttpRequestMethodNotSupportedException {
        return super.headForItemResource(resourceInformation, id, assembler);
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource<?>> getItemResource(RootResourceInformation resourceInformation,
            @BackendId Serializable id, final PersistentEntityResourceAssembler assembler,
            @RequestHeader HttpHeaders headers) throws HttpRequestMethodNotSupportedException {
        return super.getItemResource(resourceInformation, id, assembler, headers);
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<? extends ResourceSupport> putItemResource(RootResourceInformation resourceInformation,
            PersistentEntityResource payload, @BackendId Serializable id, PersistentEntityResourceAssembler assembler,
            ETag eTag, @RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
            throws HttpRequestMethodNotSupportedException {

        try {
            return super.putItemResource(resourceInformation, payload, id, assembler, eTag, acceptHeader);
        } catch (JpaObjectRetrievalFailureException ex) {
            return new ResponseEntity<Resource<?>>(HttpStatus.NOT_FOUND);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ResourceSupport> patchItemResource(RootResourceInformation resourceInformation,
            PersistentEntityResource payload, @BackendId Serializable id, PersistentEntityResourceAssembler assembler,
            ETag eTag, @RequestHeader(value = ACCEPT_HEADER, required = false) String acceptHeader)
            throws HttpRequestMethodNotSupportedException, ResourceNotFoundException {

        try {
            return super.patchItemResource(resourceInformation, payload, id, assembler, eTag, acceptHeader);
        } catch (JpaObjectRetrievalFailureException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** {@inheritDoc} */
    @Override
    @RequestMapping(value = BASE_MAPPING + "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteItemResource(RootResourceInformation resourceInformation, @BackendId Serializable id,
            ETag eTag) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
        try {
            return super.deleteItemResource(resourceInformation, id, eTag);
        } catch (JpaObjectRetrievalFailureException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
