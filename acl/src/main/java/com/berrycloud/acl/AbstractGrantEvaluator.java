package com.berrycloud.acl;

import java.io.Serializable;

import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import com.github.lothar.security.acl.grant.TypedGrantEvaluator;

public abstract class AbstractGrantEvaluator<T, ID extends Serializable>
	extends TypedGrantEvaluator<T, ID, Authentication, String> {

    @Override
    protected String mapPermission(Object permission) {
	Assert.notNull(permission, "Permission must be not null");
	return permission.toString();
    }

}
