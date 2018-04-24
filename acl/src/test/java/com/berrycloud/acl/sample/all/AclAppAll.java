package com.berrycloud.acl.sample.all;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.berrycloud.acl.configuration.EnableAclJpaRepositories;
import com.berrycloud.acl.sample.all.service.PersonService;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAclJpaRepositories
@SuppressWarnings("deprecation")
public class AclAppAll {

    @Bean
    public PersonService personService() {
        return new PersonService();
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Configuration
    public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.httpBasic().and().authorizeRequests().anyRequest().authenticated().and().csrf().disable()

            ;
        }
    }
}
