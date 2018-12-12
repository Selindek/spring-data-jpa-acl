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
package com.berrycloud.acl.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.berrycloud.acl.AclLogicImpl;
import com.berrycloud.acl.AclPersistenceUnitPostProcessor;
import com.berrycloud.acl.AclSpecification;
import com.berrycloud.acl.AclUserPermissionSpecification;
import com.berrycloud.acl.AclUtils;
import com.berrycloud.acl.configuration.rest.AclRepositoryRestConfiguration;
import com.berrycloud.acl.data.AclMetaData;
import com.berrycloud.acl.security.SimpleAclUserDetailsService;
import com.berrycloud.acl.security.access.AclPermissionEvaluator;

/**
 * Main ACL configuration class.
 *
 * @author István Rátkai (Selindek)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import(AclRepositoryRestConfiguration.class)
public class AclConfiguration {

  @Bean
  public BeanPostProcessor localContainerEntityManagerFactoryBeanPostProcessor() {
    return new BeanPostProcessor() {
      @Override

      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalContainerEntityManagerFactoryBean) {
          ((LocalContainerEntityManagerFactoryBean) bean)
              .setPersistenceUnitPostProcessors(new AclPersistenceUnitPostProcessor());
        }
        return bean;
      }
    };
  }

  @Bean
  @ConditionalOnMissingBean(UserDetailsService.class)
  public SimpleAclUserDetailsService aclUserDetailsService() {
    return new SimpleAclUserDetailsService();
  }

  @Bean
  @ConditionalOnMissingBean(PermissionEvaluator.class)
  public AclPermissionEvaluator AclPermissionEvaluator() {
    return new AclPermissionEvaluator();
  }

  @Bean
  public AclLogicImpl aclLogic() {
    return new AclLogicImpl();
  }

  @Bean
  public AclUtils aclUtils() {
    return new AclUtils();
  }

  @Bean
  public AclMetaData aclMetaData() {
    return aclLogic().createAclMetaData();
  }

  @Bean
  public AclSpecification aclSpecification() {
    return new AclUserPermissionSpecification();
  }

}
