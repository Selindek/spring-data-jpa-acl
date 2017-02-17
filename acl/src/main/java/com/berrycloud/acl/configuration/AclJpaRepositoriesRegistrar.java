package com.berrycloud.acl.configuration;

import java.lang.annotation.Annotation;

import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

class AclJpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableAclJpaRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new JpaRepositoryConfigExtension() {
//		    @Override
//		    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
//
//		        registerIfNotAlreadyRegistered(new RootBeanDefinition(AclJpaMetamodelMappingContextFactoryBean.class), registry,
//		            "jpaMappingContext", config.getSource());
//
//		        super.registerBeansForRoot(registry, config);
//
//		    }
		};
	}
}
