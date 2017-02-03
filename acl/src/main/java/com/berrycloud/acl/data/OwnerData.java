package com.berrycloud.acl.data;

import java.util.List;

public class OwnerData {

  private String attributeName;
  private List<String> permissions;

  public String getAttributeName() {
    return attributeName;
  }

  public OwnerData(String attributeName, List<String> permissions) {
    this.attributeName = attributeName;
    this.permissions = permissions;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

}
