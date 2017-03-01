package com.berrycloud.acl.domain;

import java.io.Serializable;

/**
 * This interface signs that the implementing entity is the target of the ACL.
 * Only entities implementing this interface will be checked and filtered by the ACL.
 * 
 * @author Istvan Ratkai
 *
 * @param <ID>
 */
public interface AclEntity<ID extends Serializable>{
}
