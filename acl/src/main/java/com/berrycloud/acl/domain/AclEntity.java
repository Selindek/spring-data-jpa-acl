package com.berrycloud.acl.domain;

import java.io.Serializable;

import org.springframework.hateoas.Identifiable;

import com.github.lothar.security.acl.Acl;

@Acl("aclUserStrategy")
public abstract class AclEntity<ID extends Serializable> implements Identifiable<ID>{
  
}
