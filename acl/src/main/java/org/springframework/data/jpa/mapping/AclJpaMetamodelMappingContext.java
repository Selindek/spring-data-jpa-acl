package org.springframework.data.jpa.mapping;

import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import org.springframework.data.mapping.model.AclClassGeneratingPropertyAccessorFactory;
import org.springframework.data.mapping.model.PersistentPropertyAccessorFactory;
import org.springframework.data.util.TypeInformation;

import com.berrycloud.acl.AclBeanPropertyAccessorImpl;

public class AclJpaMetamodelMappingContext extends JpaMetamodelMappingContext {

  private final PersistentPropertyAccessorFactory persistentPropertyAccessorFactory;

  public AclJpaMetamodelMappingContext(Set<Metamodel> models, AclBeanPropertyAccessorImpl aclPropertyAccessor) {
    super(models);
    persistentPropertyAccessorFactory = new AclClassGeneratingPropertyAccessorFactory(aclPropertyAccessor);
  }

  @Override
  protected JpaPersistentEntityImpl<?> addPersistentEntity(TypeInformation<?> typeInformation) {
    JpaPersistentEntityImpl<?> entity = super.addPersistentEntity(typeInformation);
    if (persistentPropertyAccessorFactory.isSupported(entity)) {
      entity.setPersistentPropertyAccessorFactory(persistentPropertyAccessorFactory);
    }
    return entity;
  }
}
