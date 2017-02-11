package org.springframework.data.mapping.model;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import com.berrycloud.acl.AclBeanPropertyAccessorImpl;

public class AclClassGeneratingPropertyAccessorFactory extends ClassGeneratingPropertyAccessorFactory{

  private AclBeanPropertyAccessorImpl aclPropertyAccessor;
  
  public AclClassGeneratingPropertyAccessorFactory(AclBeanPropertyAccessorImpl aclPropertyAccessor) {
    this.aclPropertyAccessor = aclPropertyAccessor;
  }
  
  @Override
  public PersistentPropertyAccessor getPropertyAccessor(PersistentEntity<?, ?> entity, Object bean) {
    PersistentPropertyAccessor accessor = super.getPropertyAccessor(entity, bean);
    return new AclPersistentPropertyAccessor(accessor, aclPropertyAccessor, bean);
  }
  
}
