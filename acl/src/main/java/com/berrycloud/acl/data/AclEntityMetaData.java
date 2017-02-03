package com.berrycloud.acl.data;

import java.util.ArrayList;
import java.util.List;

public class AclEntityMetaData {
  
  private List<OwnerData> ownerDataList = new ArrayList<>();

  public List<OwnerData> getOwnerDataList() {
    return ownerDataList;
  }

  public void setOwnerDataList(List<OwnerData> ownerDataList) {
    this.ownerDataList = ownerDataList;
  } 

}
