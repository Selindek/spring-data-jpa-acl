package com.berrycloud.acl;

import org.springframework.data.mapping.PersistentProperty;

public interface AclBeanPropertyAccessor {

  void setProperty(PersistentProperty<?> property, Object bean, Object value);

  Object getProperty(PersistentProperty<?> property, Object bean);

}
