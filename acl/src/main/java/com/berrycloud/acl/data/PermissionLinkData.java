package com.berrycloud.acl.data;

public class PermissionLinkData {

  private String propertyName;
  private String permissionField;

  public PermissionLinkData(String propertyName, String permissionField) {
    this.propertyName = propertyName;
    this.permissionField = permissionField;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getPermissionField() {
    return permissionField;
  }

}
