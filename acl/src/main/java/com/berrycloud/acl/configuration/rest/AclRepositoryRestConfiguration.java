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
package com.berrycloud.acl.configuration.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.BasePathAwareHandlerMapping;
import org.springframework.data.rest.webmvc.DomainPropertyClassResolver;
import org.springframework.data.rest.webmvc.ExportAwareRepositories;
import org.springframework.data.rest.webmvc.RepositoryRestHandlerMapping;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.json.JacksonMappingAwarePropertySortTranslator;
import org.springframework.data.rest.webmvc.json.MappingAwareDefaultedPageableArgumentResolver;
import org.springframework.data.rest.webmvc.json.MappingAwareDefaultedPropertyPageableArgumentResolver;
import org.springframework.data.rest.webmvc.json.MappingAwareSearchArgumentResolver;
import org.springframework.data.rest.webmvc.json.MappingAwareSortArgumentResolver;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.rest.webmvc.mapping.PageableAssociations;
import org.springframework.data.rest.webmvc.support.DelegatingHandlerMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import com.berrycloud.acl.search.SearchHandlerMethodArgumentResolver;

/**
 * This configuration class is activated only if spring-data-rest-webmvc is in the classpath. It overrides some of the
 * original beans of the the data-rest module to make it compatible with the ACL package.
 *
 * @author Will Faithfull
 * @author István Rátkai (Selindek)
 */
@Configuration
@ConditionalOnClass(value = RepositoryRestMvcConfiguration.class)
@EnableConfigurationProperties(RepositoryRestProperties.class)
@Import({ SpringBootRepositoryRestConfigurer.class })
public class AclRepositoryRestConfiguration extends RepositoryRestMvcConfiguration {

  ApplicationContext applicationContext;

  public AclRepositoryRestConfiguration(ApplicationContext context,
      ObjectFactory<ConversionService> conversionService) {
    super(context, conversionService);
    applicationContext = context;
  }

  /**
   * The PageableResolver what is constructed in the super method is not handling the domain-properties, so we have to
   * change it to a proper one here.
   */
  @Override
  protected List<HandlerMethodArgumentResolver> defaultMethodArgumentResolvers() {
    List<HandlerMethodArgumentResolver> originalList = super.defaultMethodArgumentResolvers();
    List<HandlerMethodArgumentResolver> newList = new ArrayList<>();

    JacksonMappingAwarePropertySortTranslator sortTranslator = new JacksonMappingAwarePropertySortTranslator(
        objectMapper(), repositories(), DomainPropertyClassResolver.of(repositories(), resourceMappings(), baseUri()),
        persistentEntities(), associationLinks());

    HandlerMethodArgumentResolver sortResolver = new MappingAwareSearchArgumentResolver(sortTranslator, sortResolver());

    HandlerMethodArgumentResolver defaultedPageableResolver = new MappingAwareDefaultedPropertyPageableArgumentResolver(
        sortTranslator, pageableResolver());

    for (HandlerMethodArgumentResolver element : originalList) {
      if (element instanceof MappingAwareDefaultedPageableArgumentResolver) {
        newList.add(defaultedPageableResolver);
      } else if (element instanceof MappingAwareSortArgumentResolver) {
        newList.add(sortResolver);
      } else {
        newList.add(element);
      }
    }

    return newList;
  }

  /**
   * By overriding this method we can exclude the original {@link RepositoryPropertyReferenceController} from the
   * handler-mapping.
   */
  @Override
  public DelegatingHandlerMapping restHandlerMapping() {

    Map<String, CorsConfiguration> corsConfigurations = repositoryRestConfiguration().getCorsRegistry()
        .getCorsConfigurations();

    RepositoryRestHandlerMapping repositoryMapping = new RepositoryRestHandlerMapping(resourceMappings(),
        repositoryRestConfiguration(), repositories()) {
      @Override
      protected boolean isHandler(Class<?> beanType) {
        return super.isHandler(beanType) && !beanType.getSimpleName().equals("RepositoryPropertyReferenceController")
            && !beanType.getSimpleName().equals("RepositoryEntityController");
      }
    };
    repositoryMapping.setJpaHelper(jpaHelper());
    repositoryMapping.setApplicationContext(applicationContext);
    repositoryMapping.setCorsConfigurations(corsConfigurations);
    repositoryMapping.afterPropertiesSet();

    BasePathAwareHandlerMapping basePathMapping = new BasePathAwareHandlerMapping(repositoryRestConfiguration());
    basePathMapping.setApplicationContext(applicationContext);
    basePathMapping.setCorsConfigurations(corsConfigurations);
    basePathMapping.afterPropertiesSet();

    List<HandlerMapping> mappings = new ArrayList<>();
    mappings.add(basePathMapping);
    mappings.add(repositoryMapping);

    return new DelegatingHandlerMapping(mappings);
  }

  @Override
  @Bean
  public SearchHandlerMethodArgumentResolver sortResolver() {

    return new SearchHandlerMethodArgumentResolver();
  }

  @Override
  @Bean
  public Associations associationLinks() {
    return new PageableAssociations(resourceMappings(), repositoryRestConfiguration(), pageableResolver());
  }
  
  /**
   * We replace the stock repostiories with our modified subclass. It correctly prioritises the repository interfaces,
   * so data-rest-API will use the repository with the @Primary annotation. We create the bean here in the main
   * configuration class because we use it in the PermissionEvaluator too.
   */
  @Override
  @Bean
  public Repositories repositories() {
    return new ExportAwareRepositories(applicationContext);
  }
}
