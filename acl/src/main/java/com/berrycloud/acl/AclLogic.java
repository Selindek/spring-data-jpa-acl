package com.berrycloud.acl;

import com.berrycloud.acl.security.AclUserDetails;

public interface AclLogic {

  AclUserDetails getCurrentUser();

  boolean isAdmin();

  boolean hasAuthority(String authority);

}
