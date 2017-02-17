package com.berrycloud.acl.domain;

import java.io.Serializable;

import org.springframework.hateoas.Identifiable;

//@Acl("aclStrategy")
public abstract class AclEntity<ID extends Serializable> implements Identifiable<ID>{
  
}
