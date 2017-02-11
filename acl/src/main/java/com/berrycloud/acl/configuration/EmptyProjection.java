package com.berrycloud.acl.configuration;

import org.springframework.data.rest.core.config.Projection;

import com.berrycloud.acl.domain.AclEntity;

@Projection(name = "empty", types = { AclEntity.class })
public interface EmptyProjection {
  // TODO activate this projection somehow for all the AcLEntities automatically
  // If EmptyProjection is not set as excerptProjection for a repository, that entity can be ALWAYS seen as a field or collection in associated entities
  // (The permissions are NOT applied if an entity appears as an embedded field or collection)
}
