package com.berrycloud.acl.domain;

import java.io.Serializable;


public abstract class AclEntity<ID extends Serializable> {
  
  public abstract ID getId();
}
