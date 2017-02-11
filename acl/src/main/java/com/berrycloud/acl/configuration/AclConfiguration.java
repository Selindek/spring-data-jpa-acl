/*******************************************************************************
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.berrycloud.acl.configuration;

import java.io.Serializable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder.EntityManagerFactoryBeanCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.berrycloud.acl.AclLogicImpl;
import com.berrycloud.acl.AclPersistenceUnitPostProcessor;
import com.berrycloud.acl.AclBeanPropertyAccessorImpl;
import com.berrycloud.acl.AclUserGrantEvaluator;
import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.domain.AclEntity;
import com.berrycloud.acl.security.SimpleAclUserDetailsService;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.grant.GrantEvaluatorFeature;
import com.github.lothar.security.acl.jpa.JpaSpecFeature;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AclConfiguration {

//  @Bean
//  @Order()
//  AclJpaMetamodelMappingContextFactoryBean jpaMappingContext() {
//    return new AclJpaMetamodelMappingContextFactoryBean();
//  }

  @Bean
  public SimpleAclStrategy aclStrategy() {
    return new SimpleAclStrategy();
  }

  @Bean
  public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaVendorAdapter jpaVendorAdapter,
      ObjectProvider<PersistenceUnitManager> persistenceUnitManagerProvider, JpaProperties properties) {
    EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(jpaVendorAdapter, properties.getProperties(),
        persistenceUnitManagerProvider.getIfAvailable());
    builder.setCallback(new EntityManagerFactoryBeanCallback() {

      @Override
      public void execute(LocalContainerEntityManagerFactoryBean factory) {
        factory.setPersistenceUnitPostProcessors(new AclPersistenceUnitPostProcessor());
      }

    });
    return builder;
  }

  @Bean
  @ConditionalOnMissingBean(UserDetailsService.class)
  public SimpleAclUserDetailsService aclUserDetailsService() {
    return new SimpleAclUserDetailsService();
  }

  @Bean
  public AclLogicImpl aclLogic() {
    return new AclLogicImpl();
  }

  @Bean
  public AclMetaData aclMetaData() {
    return aclLogic().createAclMetaData();
  }

  @Bean
  public AclUserPermissionSpecification aclUserPermissionSpecification(JpaSpecFeature<AclEntity<Serializable>> jpaSpecFeature) {
    AclUserPermissionSpecification aclUserPermissionSpecification = new AclUserPermissionSpecification();
    aclStrategy().install(jpaSpecFeature, aclUserPermissionSpecification);
    return aclUserPermissionSpecification;
  }

  @Bean
  public AclUserGrantEvaluator aclUserGrantEvaluator(GrantEvaluatorFeature grantEvaluatorFeature) {
    AclUserGrantEvaluator aclUserGrantEvaluator = new AclUserGrantEvaluator();
    aclStrategy().install(grantEvaluatorFeature, aclUserGrantEvaluator);
    return aclUserGrantEvaluator;
  }

  @Bean
  public AclBeanPropertyAccessorImpl aclPropertyAccessor(){
    return new AclBeanPropertyAccessorImpl();
  }
  
  
}
