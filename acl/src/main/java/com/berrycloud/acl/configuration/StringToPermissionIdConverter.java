package com.berrycloud.acl.configuration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import com.berrycloud.acl.domain.PermissionId;
import com.berrycloud.acl.domain.PermissionLink;

public class StringToPermissionIdConverter implements GenericConverter{


  private Set<ConvertiblePair> convertibleTypes = new HashSet<>();
  
  ConversionService conversionService;

  private void createConvertibleTypes(Set<Class<?>> javaTypes) {
    for(Class<?> javaType:javaTypes) {
      System.out.println(javaType);
      if (PermissionLink.class.isAssignableFrom(javaType)) {
        System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        try {
          System.out.println(javaType.getMethod("getId").getReturnType());
          convertibleTypes.add(new ConvertiblePair(String.class, javaType.getMethod("getId").getReturnType()));
        } catch (NoSuchMethodException | SecurityException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    convertibleTypes.add(new ConvertiblePair(String.class, PermissionId.class));
    
  }
  
  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    convertibleTypes.add(new ConvertiblePair(String.class, PermissionId.class));
    System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    return convertibleTypes;
  }

  @Override
  public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    System.out.println("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
    System.out.println(targetType);
    System.out.println(targetType.getResolvableType().hasGenerics());
    System.out.println(targetType.getResolvableType().hasUnresolvableGenerics());
    System.out.println(targetType.getResolvableType().getGeneric(0));


    return new PermissionId(5,4,1);
  }

}
