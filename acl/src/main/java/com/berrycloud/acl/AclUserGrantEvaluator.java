package com.berrycloud.acl;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.berrycloud.acl.domain.AclUser;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.grant.GrantEvaluatorFeature;

public class AclUserGrantEvaluator extends AbstractGrantEvaluator<AclUser<?,?>, Serializable> {

    @Autowired
    GrantEvaluatorFeature grantEvaluatorFeature;

    @Autowired
    private SimpleAclStrategy aclUserStrategy;

    @Autowired
    private EntityManager em;

    @PostConstruct
    public void init() {
	aclUserStrategy.install(grantEvaluatorFeature, this);
    }
    
    @Override
    public boolean isGranted(String permission, Authentication authentication, AclUser<?,?> domainObject) {
	System.out.println("grant evaluator...");
	return true;
    }

    @Override
    public boolean isGranted(String permission, Authentication authentication, Serializable targetId,
	    Class<? extends AclUser<?,?>> targetType) {
	return isGranted(permission, authentication, em.find(targetType, targetId));
    }
}
