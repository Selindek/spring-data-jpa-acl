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
import org.springframework.security.core.userdetails.UserDetailsService;

import com.berrycloud.acl.AclLogic;
import com.berrycloud.acl.AclPersistenceUnitPostProcessor;
import com.berrycloud.acl.AclUserGrantEvaluator;
import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.security.SimpleAclUserDetailsService;
import com.github.lothar.security.acl.SimpleAclStrategy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AclConfiguration {


  @Bean
  public SimpleAclStrategy aclUserStrategy() {
    return new SimpleAclStrategy();
  }

  @Bean
  public SimpleAclStrategy aclGroupStrategy() {
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
  public AclLogic aclLogic() {
    return new AclLogic();
  }
  
  @Bean
  public AclUserPermissionSpecification aclUserPermissionSpecification() {
    return new AclUserPermissionSpecification();
  }

  @Bean
  public AclUserGrantEvaluator aclUserGrantEvaluator() {
    return new AclUserGrantEvaluator();
  }

}
