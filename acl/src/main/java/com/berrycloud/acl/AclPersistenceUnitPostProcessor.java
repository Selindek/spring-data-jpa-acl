package com.berrycloud.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import com.berrycloud.acl.domain.AclUser;
import com.berrycloud.acl.domain.SimpleAclPermission;
import com.berrycloud.acl.domain.SimpleAclRole;

public class AclPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {
    
    private Logger LOG = LoggerFactory.getLogger(getClass());
    
    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
	// TODO Add default domains only if given domains doesn't added yet
	System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	System.out.println(pui.getManagedClassNames());
	boolean hasUser = false;
	boolean hasPermission = false;
	boolean hasRole = false;
	for(String entityName:pui.getManagedClassNames()) {
	    try {
		Class<?> entityClass = Class.forName(entityName);
		if(AclUser.class.isAssignableFrom(entityClass)) {
		    hasUser = true;
		    System.out.println("HAS USER!");
		}
	    } catch (ClassNotFoundException e) {
		// Shouldn't happen
		LOG.warn("Cannot create entity: {}",entityName);
	    }
	}
	pui.addManagedClassName(SimpleAclRole.class.getName());
	pui.addManagedClassName(SimpleAclPermission.class.getName());

    }
}
