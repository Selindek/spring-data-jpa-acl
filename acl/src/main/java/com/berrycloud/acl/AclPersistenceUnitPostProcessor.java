package com.berrycloud.acl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.domain.SimpleAclRole;
import com.berrycloud.acl.domain.SimpleAclUser;

public class AclPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

  private static Logger LOG = LoggerFactory.getLogger(AclPersistenceUnitPostProcessor.class);

  @Override
  public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
    List<Class<?>> entityClasses = createClasses(pui);

    if (missClass(entityClasses, AclUser.class)) {
      pui.addManagedClassName(SimpleAclUser.class.getName());
      LOG.info("{} was added to managed entities.", SimpleAclUser.class);
    }
    if (missClass(entityClasses, AclRole.class)) {
      pui.addManagedClassName(SimpleAclRole.class.getName());
      LOG.info("{} was added to managed entities.", SimpleAclRole.class);
    }
  }

  private static boolean missClass(List<Class<?>> entityClasses, Class<?> checkClass) {
    for (Class<?> entityClass : entityClasses) {
      if (checkClass.isAssignableFrom(entityClass)) {
        return false;
      }
    }
    return true;
  }

  private static List<Class<?>> createClasses(MutablePersistenceUnitInfo pui) {
    List<Class<?>> list = new ArrayList<Class<?>>();
    for (String entityName : pui.getManagedClassNames()) {
      try {
        list.add(Class.forName(entityName));
      } catch (ClassNotFoundException e) {
        LOG.warn("Cannot create managed entity from className: {}", entityName);
      }
    }
    return list;
  }

}
