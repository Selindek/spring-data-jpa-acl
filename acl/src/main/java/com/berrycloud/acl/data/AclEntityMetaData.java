package com.berrycloud.acl.data;

import java.util.ArrayList;
import java.util.List;

public class AclEntityMetaData {
  
  private List<OwnerData> ownerDataList = new ArrayList<>();
  private List<String> parentList = new ArrayList<>();
  private List<Class<?>> ownerPermissionList= new ArrayList<>();
  private List<Class<?>> targetPermissionList= new ArrayList<>();

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

  public List<Class<?>> getOwnerPermissionList() {
    return ownerPermissionList;
  }

  public void setOwnerPermissionList(List<Class<?>> ownerPermissionList) {
    this.ownerPermissionList = ownerPermissionList;
  }

  public List<Class<?>> getTargetPermissionList() {
    return targetPermissionList;
  }

  public void setTargetPermissionList(List<Class<?>> targetPermissionList) {
    this.targetPermissionList = targetPermissionList;
  } 

}
