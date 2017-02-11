package org.springframework.data.mapping.model;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import com.berrycloud.acl.AclBeanPropertyAccessorImpl;
import com.berrycloud.acl.domain.AclEntity;

public class AclPersistentPropertyAccessor implements PersistentPropertyAccessor {
  
  private AclBeanPropertyAccessorImpl aclPropertyAccessor;
  private PersistentPropertyAccessor delegate;
  private Object bean;
  
  public AclPersistentPropertyAccessor(PersistentPropertyAccessor delegate, AclBeanPropertyAccessorImpl aclPropertyAccessor, Object bean) {
//    System.out.println("AclPPA: "+delegate.toString());
    this.delegate = delegate;
    this.aclPropertyAccessor = aclPropertyAccessor;
    this.bean = bean;
  }

  @Override
  public void setProperty(PersistentProperty<?> property, Object value) {
//    System.out.println("AclPPA.setProperty: "+ property.getOwner().getType()+"."+property.getName()+" : "+value);
    delegate.setProperty(property, value);
  }

  @Override
  public Object getProperty(PersistentProperty<?> property) {
//    System.out.println("AclPPA.getProperty: "+ property.getOwner().getType()+"."+property.getName());
    if(AclEntity.class.isAssignableFrom(property.getActualType())) {
      System.out.println("---------------------------------> spec loader!");
      return aclPropertyAccessor.getProperty(property, bean);
    }
    return delegate.getProperty(property);
  }

  @Override
  public Object getBean() {
//    System.out.println("AclPPA.getBean");
    return delegate.getBean();
  }

}
