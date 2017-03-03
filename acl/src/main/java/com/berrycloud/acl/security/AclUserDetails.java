package com.berrycloud.acl.security;

import java.io.Serializable;

import org.springframework.security.core.userdetails.UserDetails;

public interface AclUserDetails extends UserDetails {

	Serializable getUserId();

}
