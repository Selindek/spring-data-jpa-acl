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

import static org.springframework.data.rest.webmvc.ControllerUtils.EMPTY_RESOURCE_LIST;
import static org.springframework.data.rest.webmvc.RestMediaTypes.SPRING_DATA_COMPACT_JSON_VALUE;
import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.CollectionFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.event.AfterLinkDeleteEvent;
import org.springframework.data.rest.core.event.AfterLinkSaveEvent;
import org.springframework.data.rest.core.event.BeforeLinkDeleteEvent;
import org.springframework.data.rest.core.event.BeforeLinkSaveEvent;
import org.springframework.data.rest.core.mapping.PropertyAwareResourceMapping;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.support.BackendId;
import org.springframework.data.rest.webmvc.support.DefaultedPageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.berrycloud.acl.repository.AclJpaRepository;

/**
 * @author Jon Brisbin
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author István Rátkai (Selindek)
 */
@RepositoryRestController
@SuppressWarnings({ "unchecked" })
class RepositoryAclPropertyReferenceController extends AbstractRepositoryRestController
    implements ApplicationEventPublisherAware {

  private static final String BASE_MAPPING = "/{repository}/{id}/{property}";
  private static final String COMPLEMENT = "Complement";

  private static final Collection<HttpMethod> AUGMENTING_METHODS = Arrays.asList(HttpMethod.PATCH, HttpMethod.POST);

  private final Repositories repositories;
  private final RepositoryInvokerFactory repositoryInvokerFactory;

  private ApplicationEventPublisher publisher;

  @Autowired
  public RepositoryAclPropertyReferenceController(Repositories repositories,
      RepositoryInvokerFactory repositoryInvokerFactory, PagedResourcesAssembler<Object> assembler) {

    super(assembler);

    this.repositories = repositories;
    this.repositoryInvokerFactory = repositoryInvokerFactory;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.
   * context. ApplicationEventPublisher)
   */
  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }

  @RequestMapping(value = BASE_MAPPING, method = GET)
  public ResponseEntity<ResourceSupport> followPropertyReference(final RootResourceInformation repoRequest,
      @BackendId Serializable id, final @PathVariable String property,
      final PersistentEntityResourceAssembler assembler, DefaultedPageable pageable) throws Exception {

    final HttpHeaders headers = new HttpHeaders();

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {

      if (prop.property.isMap()) {
        // No ACL for maps -> load map property directly
        Object propertyValue = prop.accessor.getProperty(prop.property);

        Map<Object, Resource<?>> resources = ((Map<Object, Object>) propertyValue).entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> assembler.toResource(e.getValue())));
        return new Resource<Object>(resources);
      }

      // Load via ACL
      Object propertyValue = findProperty(prop, pageable);

      if (prop.property.isCollectionLike()) {

        return toResources((Iterable<?>) propertyValue, assembler, prop.propertyType, Optional.empty());

      } else if (propertyValue != null) {

        PersistentEntityResource resource = assembler.toResource(propertyValue);
        headers.set("Content-Location", resource.getId().getHref());
        return resource;

      }

      throw new ResourceNotFoundException();

    };

    Optional<ResourceSupport> responseResource = doWithReferencedProperty(repoRequest, id, property, handler,
        HttpMethod.GET, pageable, null);

    return ControllerUtils.toResponseEntity(HttpStatus.OK, headers, responseResource);
  }

  @RequestMapping(value = BASE_MAPPING + COMPLEMENT, method = GET)
  public ResponseEntity<ResourceSupport> followPropertyComplementReference(final RootResourceInformation repoRequest,
      @BackendId Serializable id, final @PathVariable String property,
      final PersistentEntityResourceAssembler assembler, DefaultedPageable pageable) throws Exception {

    final HttpHeaders headers = new HttpHeaders();

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {
      if (prop.property.isCollectionLike()) {
        return toResources(findPropertyComplement(prop, pageable), assembler, prop.propertyType, Optional.empty());
      }
      throw new ResourceNotFoundException();
    };

    Optional<ResourceSupport> responseResource = doWithReferencedProperty(repoRequest, id, property, handler,
        HttpMethod.GET, pageable, COMPLEMENT);

    return ControllerUtils.toResponseEntity(HttpStatus.OK, headers, responseResource);
  }

  @RequestMapping(value = BASE_MAPPING, method = DELETE)
  public ResponseEntity<? extends ResourceSupport> deletePropertyReference(final RootResourceInformation repoRequest,
      @BackendId Serializable id, @PathVariable String property) throws Exception {

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {
      if (prop.property.isCollectionLike() || prop.property.isMap()) {
        throw HttpRequestMethodNotSupportedException.forRejectedMethod(HttpMethod.DELETE)
            .withAllowedMethods(HttpMethod.GET, HttpMethod.HEAD);
      }

      Object propertyValue = findProperty(prop, (DefaultedPageable) null);

      if (propertyValue != null) {
        prop.wipeValue();

        // TODO improve events
        publisher.publishEvent(new BeforeLinkDeleteEvent(prop.accessor.getBean(), propertyValue));
        Object result = prop.propertyRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
        publisher.publishEvent(new AfterLinkDeleteEvent(result, propertyValue));
      }

      return null;

    };

    doWithReferencedProperty(repoRequest, id, property, handler, HttpMethod.DELETE, null, null);

    return ControllerUtils.toEmptyResponse(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(value = BASE_MAPPING + "/{propertyId}", method = GET)
  public ResponseEntity<ResourceSupport> followPropertyReference(final RootResourceInformation repoRequest,
      @BackendId Serializable id, @PathVariable String property, final @PathVariable String propertyId,
      final PersistentEntityResourceAssembler assembler) throws Exception {

    final HttpHeaders headers = new HttpHeaders();

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {

      if (prop.property.isMap()) {
        Object propertyValue = prop.accessor.getProperty(prop.property);
        propertyValue = ((Map<String, Object>) propertyValue).get(propertyId);
        if (propertyValue == null) {
          throw new ResourceNotFoundException();
        }
        PersistentEntityResource resource = assembler.toResource(propertyValue);
        headers.set("Content-Location", resource.getId().getHref());
        return resource;
      }

      Object propertyValue = findProperty(prop, propertyId);
      if (propertyValue != null) {
        PersistentEntityResource resource = assembler.toResource(propertyValue);
        String href = resource.getId().getHref();
        headers.set("Content-Location", href);

        if (prop.property.isCollectionLike() || href.substring(href.lastIndexOf('/') + 1).equals(propertyId)) {
          return resource;
        }
      }
      throw new ResourceNotFoundException();
    };

    Optional<ResourceSupport> responseResource = doWithReferencedProperty(repoRequest, id, property, handler,
        HttpMethod.GET, null, propertyId);
    return ControllerUtils.toResponseEntity(HttpStatus.OK, headers, responseResource);
  }

  @RequestMapping(value = BASE_MAPPING, method = GET, produces = { SPRING_DATA_COMPACT_JSON_VALUE,
      TEXT_URI_LIST_VALUE })
  public ResponseEntity<ResourceSupport> followPropertyReferenceCompact(RootResourceInformation repoRequest,
      @BackendId Serializable id, @PathVariable String property, PersistentEntityResourceAssembler assembler,
      DefaultedPageable pageable) throws Exception {

    return createCompactResponse(followPropertyReference(repoRequest, id, property, assembler, pageable));
  }

  private ResponseEntity<ResourceSupport> createCompactResponse(ResponseEntity<ResourceSupport> response) {

    if (response.getStatusCode() != HttpStatus.OK) {
      return response;
    }

    ResourceSupport resource = response.getBody();

    List<Link> links = new ArrayList<>();

    if (resource instanceof Resources) {

      for (Resource<?> res : ((Resources<Resource<?>>) resource).getContent()) {
        links.add(res.getLink("self"));
      }
      if (resource instanceof PagedResources) {
        return ControllerUtils.toResponseEntity(HttpStatus.OK, HttpHeaders.EMPTY,
            new PagedResources<>(Collections.emptyList(), ((PagedResources<?>) resource).getMetadata(), links));
      }
    } else {

      Object content = ((Resource<?>) resource).getContent();
      if (content instanceof Map) {

        Map<Object, Resource<?>> map = (Map<Object, Resource<?>>) content;

        for (Entry<Object, Resource<?>> entry : map.entrySet()) {
          Link l = new Link(entry.getValue().getLink("self").getHref(), entry.getKey().toString());
          links.add(l);
        }
      } else {
        links.add(resource.getLink("self"));
      }
    }
    return ControllerUtils.toResponseEntity(HttpStatus.OK, HttpHeaders.EMPTY,
        new Resources<>(EMPTY_RESOURCE_LIST, links));
  }

  @RequestMapping(value = BASE_MAPPING + COMPLEMENT, method = GET, produces = { SPRING_DATA_COMPACT_JSON_VALUE,
      TEXT_URI_LIST_VALUE })
  public ResponseEntity<ResourceSupport> followPropertyComplementReferenceCompact(RootResourceInformation repoRequest,
      @BackendId Serializable id, @PathVariable String property, PersistentEntityResourceAssembler assembler,
      DefaultedPageable pageable) throws Exception {

    return createCompactResponse(followPropertyComplementReference(repoRequest, id, property, assembler, pageable));
  }

  @RequestMapping(value = BASE_MAPPING, method = { PATCH, PUT, POST }, //
      consumes = { MediaType.APPLICATION_JSON_VALUE, SPRING_DATA_COMPACT_JSON_VALUE, TEXT_URI_LIST_VALUE })
  public ResponseEntity<? extends ResourceSupport> createPropertyReference(
      final RootResourceInformation resourceInformation, final HttpMethod requestMethod,
      final @RequestBody(required = false) Resources<Object> incoming, @BackendId Serializable id,
      @PathVariable String property) throws Exception {

    final Resources<Object> source = incoming == null ? new Resources<>(Collections.emptyList()) : incoming;

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {

      Object propertyValue = prop.accessor.getProperty(prop.property);

      Class<?> propertyType = prop.property.getType();

      if (prop.property.isCollectionLike()) {
        Collection<Object> collection = AUGMENTING_METHODS.contains(requestMethod) ? (Collection<Object>) propertyValue
            : CollectionFactory.createCollection(propertyType, 0);

        // Add to the existing collection
        for (Link l : source.getLinks()) {
          Object value = loadPropertyValue(prop.propertyType, l);
          if (value != null) {
            collection.add(value);
          }
        }
        propertyValue = collection;

      } else if (prop.property.isMap()) {

        Map<String, Object> map = AUGMENTING_METHODS.contains(requestMethod) ? (Map<String, Object>) propertyValue
            : CollectionFactory.<String, Object> createMap(propertyType, 0);

        // Add to the existing collection
        for (Link l : source.getLinks()) {
          Object value = loadPropertyValue(prop.propertyType, l);
          if (value != null) {
            map.put(l.getRel(), value);
          }
        }
        propertyValue = map;

      } else {

        if (HttpMethod.PATCH.equals(requestMethod)) {
          throw HttpRequestMethodNotSupportedException.forRejectedMethod(HttpMethod.PATCH)//
              .withAllowedMethods(HttpMethod.PATCH)//
              .withMessage(
                  "Cannot PATCH a reference to this singular property since the property type is not a List or a Map.");
        }

        if (source.getLinks().size() != 1) {
          throw new IllegalArgumentException(
              "Must send only 1 link to update a property reference that isn't a List or a Map.");
        }

        propertyValue = loadPropertyValue(prop.propertyType, source.getLinks().get(0));
        if (propertyValue == null) {
          return null;
        }
      }

      prop.accessor.setProperty(prop.property, propertyValue);

      // TODO improve events
      publisher.publishEvent(new BeforeLinkSaveEvent(prop.accessor.getBean(), propertyValue));
      Object result = prop.propertyRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
      publisher.publishEvent(new AfterLinkSaveEvent(result, propertyValue));

      return null;

    };

    doWithReferencedProperty(resourceInformation, id, property, handler, requestMethod, null, null);

    return ControllerUtils.toEmptyResponse(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(value = BASE_MAPPING + "/{propertyId}", method = DELETE)
  public ResponseEntity<ResourceSupport> deletePropertyReferenceId(final RootResourceInformation repoRequest,
      @BackendId Serializable backendId, @PathVariable String property, final @PathVariable String propertyId)
      throws Exception {

    Function<ReferencedProperty, ResourceSupport> handler = prop -> {

      Object propertyValue = prop.accessor.getProperty(prop.property);
      if (propertyValue == null) {
        return null;
      }
      if (prop.property.isCollectionLike()) {
        Object value = findProperty(prop, propertyId);
        if (value == null) {
          return null;
        }
        ((Collection<Object>) propertyValue).remove(value);

      } else if (prop.property.isMap()) {
        ((Map<String, Object>) propertyValue).remove(propertyId);

      } else {
        prop.wipeValue();
        propertyValue = null;
      }

      // TODO improve events
      publisher.publishEvent(new BeforeLinkDeleteEvent(prop.accessor.getBean(), propertyValue));
      Object result = prop.propertyRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
      publisher.publishEvent(new AfterLinkDeleteEvent(result, propertyValue));

      return null;

    };

    doWithReferencedProperty(repoRequest, backendId, property, handler, HttpMethod.DELETE, null, propertyId);

    return ControllerUtils.toEmptyResponse(HttpStatus.NO_CONTENT);
  }

  private Object loadPropertyValue(Class<?> type, Link link) {

    String href = link.expand().getHref();
    String id = href.substring(href.lastIndexOf('/') + 1);

    RepositoryInvoker invoker = repositoryInvokerFactory.getInvokerFor(type);

    return invoker.invokeFindById(id).orElse(null);
  }

  private Optional<ResourceSupport> doWithReferencedProperty(RootResourceInformation resourceInformation,
      Serializable id, String propertyPath, Function<ReferencedProperty, ResourceSupport> handler, HttpMethod method,
      DefaultedPageable pageable, String propertyId) throws Exception {

    ResourceMetadata metadata = resourceInformation.getResourceMetadata();
    PropertyAwareResourceMapping mapping = metadata.getProperty(propertyPath);

    if (mapping == null || !mapping.isExported()) {
      throw new ResourceNotFoundException();
    }

    PersistentProperty<?> property = mapping.getProperty();
    resourceInformation.verifySupportedMethod(method, property);

    // We first load the parent domainObject and check its accessibility
    AclJpaRepository<Object, Object> propertyRepository = getAclRepository(property.getOwner().getType());
    Object domainObj = propertyRepository.findById(id, HttpMethod.GET.equals(method) ? "read" : "update")
        .orElseThrow(() -> new ResourceNotFoundException());

    PersistentPropertyAccessor<Object> accessor = property.getOwner().getPropertyAccessor(domainObj);

    return Optional.ofNullable(handler.apply(new ReferencedProperty(propertyRepository, property, accessor)));
  }

  protected AclJpaRepository<Object, Object> getAclRepository(Class<?> type) {
    Object repository = repositories.getRepositoryFor(type).orElseThrow(() -> new ResourceNotFoundException());
    if (!AclJpaRepository.class.isAssignableFrom(AopUtils.getTargetClass(repository))) {
      throw new ResourceNotFoundException();
    }
    return (AclJpaRepository<Object, Object>) repository;
  }

  protected Object findProperty(ReferencedProperty prop, String propertyId) {

    Object ownerId = prop.accessor.getProperty(prop.property.getOwner().getIdProperty());
    // find the property as an object
    return prop.propertyRepository.findProperty(ownerId, prop.property, propertyId);
  }

  protected Object findProperty(ReferencedProperty prop, DefaultedPageable pageable) {

    Object ownerId = prop.accessor.getProperty(prop.property.getOwner().getIdProperty());

    // find the property as a collection
    Pageable page = pageable == null ? Pageable.unpaged() : pageable.getPageable();
    return prop.propertyRepository.findProperty(ownerId, prop.property, page);
  }

  protected Page<Object> findPropertyComplement(ReferencedProperty prop, DefaultedPageable pageable) {

    Object ownerId = prop.accessor.getProperty(prop.property.getOwner().getIdProperty());

    // find the property as a collection-complement
    Pageable page = pageable == null ? Pageable.unpaged() : pageable.getPageable();
    return (Page<Object>) prop.propertyRepository.findPropertyComplement(ownerId, prop.property, page);
  }

  private static class ReferencedProperty {

    final AclJpaRepository<Object, Object> propertyRepository;
    final PersistentProperty<?> property;
    final Class<?> propertyType;
    final PersistentPropertyAccessor<Object> accessor;

    private ReferencedProperty(AclJpaRepository<Object, Object> propertyRepository, PersistentProperty<?> property,
        PersistentPropertyAccessor<Object> wrapper) {

      this.propertyRepository = propertyRepository;
      this.property = property;
      this.accessor = wrapper;
      this.propertyType = property.getActualType();
    }

    public void wipeValue() {
      accessor.setProperty(property, null);
    }

  }

  @ExceptionHandler
  public ResponseEntity<Void> handle(HttpRequestMethodNotSupportedException exception) {
    return exception.toResponse();
  }

  static class HttpRequestMethodNotSupportedException extends RuntimeException {

    private static final long serialVersionUID = 3704212056962845475L;

    private final HttpMethod rejectedMethod;
    private final HttpMethod[] allowedMethods;
    private final String message;

    private HttpRequestMethodNotSupportedException(HttpMethod rejectedMethod, HttpMethod[] allowedMethods,
        String message) {
      this.rejectedMethod = rejectedMethod;
      this.allowedMethods = allowedMethods;
      this.message = message;
    }

    public static HttpRequestMethodNotSupportedException forRejectedMethod(HttpMethod method) {
      return new HttpRequestMethodNotSupportedException(method, new HttpMethod[0], null);
    }

    public HttpRequestMethodNotSupportedException withAllowedMethods(HttpMethod... methods) {
      return new HttpRequestMethodNotSupportedException(this.rejectedMethod, methods.clone(), null);
    }

    public HttpRequestMethodNotSupportedException withMessage(String message, Object... parameters) {
      return new HttpRequestMethodNotSupportedException(this.rejectedMethod, this.allowedMethods,
          String.format(message, parameters));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
      return message;
    }

    public ResponseEntity<Void> toResponse() {
      return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).allow(allowedMethods).build();
    }
  }
}
