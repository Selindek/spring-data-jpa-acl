package com.berrycloud.acl.configuration.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.webmvc.DomainPropertyClassResolver;
import org.springframework.data.rest.webmvc.ExportAwareRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.json.JacksonMappingAwarePropertySortTranslator;
import org.springframework.data.rest.webmvc.json.MappingAwareDefaultedPageableArgumentResolver;
import org.springframework.data.rest.webmvc.json.MappingAwareDefaultedPropertyPageableArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;


/**
 * This configuration class is activated only if spring-data-rest-webmvc is in the classpath
 * 
 * @author Will Faithfull
 * @author István Rátkai
 */
@Configuration
@ConditionalOnClass(value = RepositoryRestMvcConfiguration.class)
public class RepositoryRestConfiguration extends RepositoryRestMvcConfiguration {

	@Autowired
	ApplicationContext context;

	/**
	 * We replace the stock repostiories with our modified subclass. It correctly prioritises the repository interfaces
	 */
	@Override
	public Repositories repositories() {
		return new ExportAwareRepositories(context);
	}

	/**
	 * Register a simple filter what adds the _aclProperty=true header to all requests. This way the modified
	 * RepositoryAclPropertyReferenceController's methods will be used instead of the original RepositoryPropertyReferenceController's ones.
	 * Unfortunately there is no other way to turn off or invalidate the registered controller-methods. We need this filter when
	 * data-rest-webmvc is in the classpath
	 */
	@Bean
	public AclPropertyFilter aclPropertyFilter() {
		return new AclPropertyFilter();
	}

	/**
	 * The PageableResolver what is constructed in the super method is not handling the domain-properties, so we have to
	 * change it to a proper one here.
	 */
	@Override
	protected List<HandlerMethodArgumentResolver> defaultMethodArgumentResolvers() {
		List<HandlerMethodArgumentResolver> originalList = super.defaultMethodArgumentResolvers();
		List<HandlerMethodArgumentResolver> newList = new ArrayList<>();
		
		JacksonMappingAwarePropertySortTranslator sortTranslator = new JacksonMappingAwarePropertySortTranslator(objectMapper(),
				repositories(), DomainPropertyClassResolver.of(repositories(), resourceMappings(), baseUri()), persistentEntities(),
				associationLinks());

		HandlerMethodArgumentResolver defaultedPageableResolver = new MappingAwareDefaultedPropertyPageableArgumentResolver(
				sortTranslator, pageableResolver());
		
		for(HandlerMethodArgumentResolver element:originalList) {
			if(element instanceof MappingAwareDefaultedPageableArgumentResolver) {
				newList.add(defaultedPageableResolver);
			} else {
				newList.add(element);
			}
		}

		return newList;
	}
}
