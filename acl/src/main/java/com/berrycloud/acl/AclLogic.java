package com.berrycloud.acl;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.berrycloud.acl.domain.AclRole;
import com.berrycloud.acl.domain.AclUser;


public class AclLogic {

    @Autowired
    private EntityManager em;
    
    private Class<AclUser<Serializable,AclRole<Serializable>>> aclUserType;
    
   
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init(){
	System.out.println("XXXXXXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	for(EntityType<?> et:em.getMetamodel().getEntities()) {
	    Class<?> type =et.getJavaType();
	    if(AclUser.class.isAssignableFrom(type)) {
		aclUserType=  (Class<AclUser<Serializable,AclRole<Serializable>>>) type;
	    }
	    
	    System.out.println(et.getJavaType());

	    System.out.println("-------------------");

	}

	Assert.notNull(aclUserType, "AclUser entity cannot be found");
	System.out.println("XXXXXXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	
    }

//     private Class<?> checkEntityType(Class<?> entityType, Class<?> checkType, Class<?> foundType) {
//	if(checkType.isAssignableFrom(entityType));
//	    
//	return null;
//    }
    

    public Class<AclUser<Serializable,AclRole<Serializable>>> getAclUserType() {
        return aclUserType;
    }


}
