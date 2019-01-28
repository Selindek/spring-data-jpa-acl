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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.support.Repositories;
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

import com.berrycloud.acl.AclConstants;
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
  private final ConversionService conversionService;

  private ApplicationEventPublisher publisher;

  @Autowired
  public RepositoryAclPropertyReferenceController(Repositories repositories, PagedResourcesAssembler<Object> assembler,
      ConversionService defaultConversionService) {

    super(assembler);

    this.repositories = repositories;
    this.conversionService = defaultConversionService;
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
        if (AclConstants.UPDATE_PERMISSION == prop.requiredPermission) {
          // OneToOne property without UPDATE permission on this side -> reload with UPDATE permission
          propertyValue = prop.propertyRepository
              .findById(repositories.getEntityInformationFor(prop.propertyType).getId(propertyValue),
                  AclConstants.UPDATE_PERMISSION)
              .orElseThrow(ResourceNotFoundException::new);

        }
        prop.wipeValue();

        PropertyReference pr = new PropertyReference(prop.property.getName(), propertyValue);

        publisher.publishEvent(new BeforeLinkDeleteEvent(prop.accessor.getBean(), pr));
        Object result = prop.parentRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
        publisher.publishEvent(new AfterLinkDeleteEvent(result, pr));
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

      Object deletedObjects = null;
      Object addedObjects = null;

      if (prop.property.isCollectionLike()) {
        // original set
        Collection<Object> collection = (Collection<Object>) propertyValue;
        // objects to add (load only objects with proper permission)
        List<Object> addedSet = loadPropertyValues(prop, source.getLinks());

        Iterator<Object> iterator = addedSet.iterator();

        if (AUGMENTING_METHODS.contains(requestMethod)) { // POST and PATCH
          // helper set
          Set<Object> set = new HashSet<>(collection);
          while (iterator.hasNext()) {
            Object value = iterator.next();
            if (set.contains(value)) {
              iterator.remove();
            } else {
              collection.add(value);
            }
          }
        } else { // PUT
          // Reload existing elements with permission-check
          // helper set will contain only elements which the current user can delete
          Set<Object> set = new HashSet<>(reloadPropertyValues(prop, collection));

          while (iterator.hasNext()) {
            Object value = iterator.next();
            if (set.remove(value)) {
              iterator.remove();
            }
          }
          if (!set.isEmpty()) {
            deletedObjects = set;
            collection.removeAll(set);
          }
          collection.addAll(addedSet);
        }

        if (!addedSet.isEmpty()) {
          addedObjects = addedSet;
        }

      } else if (prop.property.isMap()) {
        // original map
        Map<String, Object> map = (Map<String, Object>) propertyValue;
        // entries to remove
        Map<String, Object> removedMap = new HashMap<>();
        // entries to add
        Map<String, Object> addedMap = new HashMap<>();
        source.getLinks().stream().forEach(l -> {
          Object value = loadPropertyValue(prop, l);
          if (value != null) {
            addedMap.put(l.getRel(), value);
          }
        });

        Iterator<Entry<String, Object>> iterator = addedMap.entrySet().iterator();

        if (AUGMENTING_METHODS.contains(requestMethod)) { // POST and PATCH
          while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            Object oldValue = map.put(entry.getKey(), entry.getValue());
            if (oldValue != null) {
              // has value with a same key
              if (oldValue == entry.getValue()) {
                // same value -> no need to add
                iterator.remove();
              } else {
                // different value -> remove old
                removedMap.put(entry.getKey(), oldValue);
              }
            }
          }
        } else { // PUT
          // remove all original entries, except if...
          removedMap.putAll(map);
          map.clear();
          map.putAll(addedMap);
          while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            Object oldValue = removedMap.get(entry.getKey());
            // ...has entry with a same value...
            if (oldValue == entry.getValue()) {
              // ...then no need to remove
              removedMap.remove(entry.getKey());
              // ... and no need to add
              iterator.remove();
            }
          }
        }

        if (!addedMap.isEmpty()) {
          addedObjects = addedMap;
        }
        if (!removedMap.isEmpty()) {
          deletedObjects = removedMap;
        }

      } else {
        // Single-value property
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

        // Old property (could be null)
        deletedObjects = propertyValue;
        propertyValue = loadPropertyValue(prop, source.getLinks().get(0));
        if (propertyValue == null) {
          // if new property cannot be loaded (either doesn't exist or no permission)
          throw new ResourceNotFoundException();
        }
        // New property
        addedObjects = propertyValue;
      }

      prop.accessor.setProperty(prop.property, propertyValue);

      Optional<PropertyReference> prd = Optional.ofNullable(deletedObjects)
          .map(o -> new PropertyReference(prop.property.getName(), o));
      Optional<PropertyReference> prs = Optional.ofNullable(addedObjects)
          .map(o -> new PropertyReference(prop.property.getName(), o));
      Object parent = prop.accessor.getBean();

      prd.ifPresent(o -> publisher.publishEvent(new BeforeLinkDeleteEvent(parent, o)));
      prs.ifPresent(o -> publisher.publishEvent(new BeforeLinkSaveEvent(parent, o)));
      Object result = prop.parentRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
      prd.ifPresent(o -> publisher.publishEvent(new AfterLinkDeleteEvent(result, o)));
      prs.ifPresent(o -> publisher.publishEvent(new AfterLinkSaveEvent(result, o)));

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

      Object deletedObject = null;

      if (prop.property.isCollectionLike()) {
        Object propertyValue = prop.accessor.getProperty(prop.property);
        // Object value = findProperty(prop, propertyId);
        Object value = loadPropertyValue(prop, propertyId);
        if (value != null && ((Collection<Object>) propertyValue).remove(value)) {
          deletedObject = Collections.singleton(value);
        }

      } else if (prop.property.isMap()) {
        Object propertyValue = prop.accessor.getProperty(prop.property);
        Object removed = ((Map<String, Object>) propertyValue).remove(propertyId);
        if (removed != null) {
          HashMap<String, Object> map = new HashMap<>();
          map.put(propertyId, removed);
          deletedObject = map;
        }
      } else {
        // load value with required permission
        Object propertyValue = loadPropertyValue(prop, propertyId);
        if (propertyValue != null) {
          prop.wipeValue();
          deletedObject = propertyValue;
        }
      }

      if (deletedObject != null) {
        PropertyReference pr = new PropertyReference(prop.property.getName(), deletedObject);

        publisher.publishEvent(new BeforeLinkDeleteEvent(prop.accessor.getBean(), pr));
        Object result = prop.parentRepository.saveWithoutPermissionCheck(prop.accessor.getBean());
        publisher.publishEvent(new AfterLinkDeleteEvent(result, pr));
      }
      return null;

    };

    doWithReferencedProperty(repoRequest, backendId, property, handler, HttpMethod.DELETE, null, propertyId);

    return ControllerUtils.toEmptyResponse(HttpStatus.NO_CONTENT);
  }

  private Object loadPropertyValue(ReferencedProperty prop, Link link) {
    return loadPropertyValue(prop, linkToString(link));
  }

  private String linkToString(Link link) {
    String href = link.expand().getHref();
    return href.substring(href.lastIndexOf('/') + 1);
  }

  private Object loadPropertyValue(ReferencedProperty prop, String id) {
    Object typedId = conversionService.convert(id, prop.idType);

    return prop.propertyRepository.findById(typedId, prop.requiredPermission).orElse(null);
  }

  private List<Object> reloadPropertyValues(ReferencedProperty prop, Collection<Object> collection) {

    EntityInformation<Object, Object> ei = repositories.getEntityInformationFor(prop.propertyType);

    List<Object> ids = collection.stream().map(s -> ei.getId(s)).collect(Collectors.toList());

    return prop.propertyRepository.findAllById(ids, prop.requiredPermission);
  }

  private List<Object> loadPropertyValues(ReferencedProperty prop, Collection<Link> links) {

    List<Object> ids = links.stream().map(this::linkToString).map(s -> conversionService.convert(s, prop.idType))
        .collect(Collectors.toList());

    return prop.propertyRepository.findAllById(ids, prop.requiredPermission);
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

    String propertyPermission = AclConstants.READ_PERMISSION;
    Optional<Object> domainObj = Optional.empty();

    // We first load the parent domainObject and check the permissions needed
    AclJpaRepository<Object, Object> parentRepository = getAclRepository(property.getOwner().getType());

    if (!HttpMethod.GET.equals(method)) {
      // POST/PATCH/PUT/DELETE -> UPDATE permission is needed
      if (property.isAnnotationPresent(OneToMany.class)) {
        // Other side is a singular property: need UPDATE permission only to the other side of the association
        propertyPermission = AclConstants.UPDATE_PERMISSION;
      } else {
        domainObj = parentRepository.findById(id, AclConstants.UPDATE_PERMISSION);
        if (!domainObj.isPresent()) {
          if (property.isAnnotationPresent(ManyToOne.class)) {
            // Singular property reference : UPDATE permission is mandatory on this side
            throw new ResourceNotFoundException();
          }
          if (property.isMap()) {
            // Map: UPDATE permission is mandatory on this side
            throw new ResourceNotFoundException();
          }
          // No UPDATE permission on this side: need UPDATE on the other side
          propertyPermission = AclConstants.UPDATE_PERMISSION;
        }
      }
    }
    if (!domainObj.isPresent()) {
      domainObj = parentRepository.findById(id, AclConstants.READ_PERMISSION);
    }
    if (!domainObj.isPresent()) {
      throw new ResourceNotFoundException();
    }

    PersistentPropertyAccessor<Object> accessor = property.getOwner().getPropertyAccessor(domainObj.get());
    AclJpaRepository<Object, Object> propertyRepository = getAclRepository(property.getActualType());
    Class<Object> idType = repositories.getEntityInformationFor(property.getActualType()).getIdType();

    return Optional.ofNullable(handler.apply(
        new ReferencedProperty(parentRepository, propertyRepository, property, accessor, propertyPermission, idType)));
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
    return prop.parentRepository.findProperty(ownerId, prop.property, propertyId);
  }

  protected Object findProperty(ReferencedProperty prop, DefaultedPageable pageable) {

    Object ownerId = prop.accessor.getProperty(prop.property.getOwner().getIdProperty());

    // find the property as a collection
    Pageable page = pageable == null ? Pageable.unpaged() : pageable.getPageable();
    return prop.parentRepository.findProperty(ownerId, prop.property, page);
  }

  protected Page<Object> findPropertyComplement(ReferencedProperty prop, DefaultedPageable pageable) {

    Object ownerId = prop.accessor.getProperty(prop.property.getOwner().getIdProperty());

    // find the property as a collection-complement
    Pageable page = pageable == null ? Pageable.unpaged() : pageable.getPageable();
    return (Page<Object>) prop.parentRepository.findPropertyComplement(ownerId, prop.property, page);
  }

  private static class ReferencedProperty {

    final AclJpaRepository<Object, Object> propertyRepository;
    final AclJpaRepository<Object, Object> parentRepository;
    final PersistentProperty<?> property;
    final Class<?> propertyType;
    final PersistentPropertyAccessor<Object> accessor;
    final String requiredPermission;
    final Class<Object> idType;

    private ReferencedProperty(AclJpaRepository<Object, Object> parentRepository,
        AclJpaRepository<Object, Object> propertyRepository, PersistentProperty<?> property,
        PersistentPropertyAccessor<Object> wrapper, String requiredPermission, Class<Object> idType) {

      this.parentRepository = parentRepository;
      this.propertyRepository = propertyRepository;
      this.property = property;
      this.accessor = wrapper;
      this.propertyType = property.getActualType();
      this.requiredPermission = requiredPermission;
      this.idType = idType;
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
