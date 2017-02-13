package com.berrycloud.acl.data;

import java.util.ArrayList;
import java.util.List;

public class AclEntityMetaData {
  
  private List<OwnerData> ownerDataList = new ArrayList<>();
  private List<String> parentList = new ArrayList<>();
  private List<String> permissionLinkOwnerList= new ArrayList<>();
  private List<String> permissionLinkTargetList= new ArrayList<>();

  public List<OwnerData> getOwnerDataList() {
    return ownerDataList;
  }

  public void setOwnerDataList(List<OwnerData> ownerDataList) {
    this.ownerDataList = ownerDataList;
  }

  public List<String> getParentList() {
    return parentList;
  }

  public void setParentList(List<String> parentList) {
    this.parentList = parentList;
  }

  public List<String> getPermissionLinkOwnerList() {
    return permissionLinkOwnerList;
  }

  public void setPermissionLinkOwnerList(List<String> permissionLinkOwnerList) {
    this.permissionLinkOwnerList = permissionLinkOwnerList;
  }

  public List<String> getPermissionLinkTargetList() {
    return permissionLinkTargetList;
  }

  public void setPermissionLinkTargetList(List<String> permissionLinkTargetList) {
    this.permissionLinkTargetList = permissionLinkTargetList;
  }

}
