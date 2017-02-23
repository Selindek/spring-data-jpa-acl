package com.berrycloud.acl.data;

public class OwnerData extends PropertyPermissionData{

  private boolean collection;

  public OwnerData(String propertyName, boolean collection, String[] permissions ) {
    super(propertyName, permissions);
    this.collection = collection;
  }

  public boolean isCollection() {
    return collection;
  }

}
