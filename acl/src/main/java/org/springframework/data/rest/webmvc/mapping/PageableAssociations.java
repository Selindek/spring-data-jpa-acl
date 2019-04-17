/*
 * Copyright 2014-2017 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.rest.core.Path;
import org.springframework.data.rest.core.config.ProjectionDefinitionConfiguration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMapping;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.berrycloud.acl.annotation.AclLinkRole;
import com.berrycloud.acl.security.AclUserDetailsService;

/**
 * A value object to for {@link Link}s representing associations.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Haroun Pacquee
 * @author István Rátkai (Selindek)
 * @since 2.1
 */
public class PageableAssociations extends Associations {

  private static final String COMPLEMENT = "Complement";

  private final ResourceMappings mappings;
  private final RepositoryRestConfiguration config;
  private final HateoasPageableHandlerMethodArgumentResolver pageableResolver;

  public PageableAssociations(ResourceMappings mappings, RepositoryRestConfiguration config,
      HateoasPageableHandlerMethodArgumentResolver pageableResolver) {
    super(mappings, config);
    this.mappings = mappings;
    this.config = config;
    this.pageableResolver = pageableResolver;
  }

  /**
   * Returns the links to render for the given {@link Association}.
   *
   * @param association
   *          must not be {@literal null}.
   * @param path
   *          must not be {@literal null}.
   * @return
   */
  /*
   * We have to recreate this whole method because it calls a private method in the superclass
   */
  @Override
  public List<Link> getLinksFor(Association<? extends PersistentProperty<?>> association, Path path) {

    Assert.notNull(association, "Association must not be null!");
    Assert.notNull(path, "Base path must not be null!");

    if (isLinkableAssociation(association) && isAllowedByLinkRole(association)) {

      PersistentProperty<?> property = association.getInverse();
      ResourceMetadata metadata = mappings.getMetadataFor(property.getOwner().getType());
      ResourceMapping propertyMapping = metadata.getMappingFor(property);

      String href = path.slash(propertyMapping.getPath()).toString();

      if (!association.getInverse().isCollectionLike()) {
        UriTemplate template = new UriTemplate(href, getProjectionVariable(property));

        return Collections.singletonList(new Link(template, propertyMapping.getRel()));
      }
      UriComponents components = UriComponentsBuilder.fromUriString(href).build();

      TemplateVariables variables = pageableResolver.getPaginationTemplateVariables(null, components)
          .concat(getProjectionVariable(property));

      UriTemplate template = new UriTemplate(href, variables);
      Link link = new Link(template, propertyMapping.getRel());

      if (association.getInverse().findPropertyOrOwnerAnnotation(HideComplementEndpoint.class) == null) {
        // Add link to the complement-collection too
        return Arrays.asList(link,
            new Link(new UriTemplate(href + COMPLEMENT, variables), propertyMapping.getRel() + COMPLEMENT));
      }
      return Collections.singletonList(link);

    }

    return Collections.emptyList();
  }

  private boolean isAllowedByLinkRole(Association<? extends PersistentProperty<?>> association) {
    AclLinkRole annotation = association.getInverse().findAnnotation(AclLinkRole.class);
    return annotation == null || AclUserDetailsService.hasAnyAuthorities(annotation.value());
  }

  /*
   * We have to recreate this whole method because it is a private method in the superclass
   */
  private TemplateVariables getProjectionVariable(PersistentProperty<?> property) {

    ProjectionDefinitionConfiguration projectionConfiguration = config.getProjectionConfiguration();

    return projectionConfiguration.hasProjectionFor(property.getActualType()) //
        ? new TemplateVariables(new TemplateVariable(projectionConfiguration.getParameterName(), REQUEST_PARAM)) //
        : TemplateVariables.NONE;
  }

}
