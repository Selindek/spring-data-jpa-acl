package com.berrycloud.acl;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.berrycloud.acl.domain.AclUser;

public class AclUserGrantEvaluator extends AbstractGrantEvaluator<AclUser<?, ?>, Serializable> {

  private static Logger LOG = LoggerFactory.getLogger(AclUserGrantEvaluator.class);

  @Autowired
  private EntityManager em;

  @Override
  public boolean isGranted(String permission, Authentication authentication, AclUser<?, ?> domainObject) {
    LOG.warn("GrantEvaluator is not implemented yet. Access granted by default.");
    return true;
  }

  @Override
  public boolean isGranted(String permission, Authentication authentication, Serializable targetId, Class<? extends AclUser<?, ?>> targetType) {
    return isGranted(permission, authentication, em.find(targetType, targetId));
  }
}
