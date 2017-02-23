package com.berrycloud.acl.data;

import static com.berrycloud.acl.AclUtils.ALL_PERMISSION;
import static com.berrycloud.acl.AclUtils.PERMISSION_PREFIX_DELIMITER;
import static com.berrycloud.acl.AclUtils.READ_PERMISSION;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PermissionData {

  private Set<String> permissions;
  private Set<String> allPrefixes;

  public PermissionData(String[] permissions) {
    this.permissions = new HashSet<String>(Arrays.asList(permissions));
    this.allPrefixes = new HashSet<String>();
    calculatePermissions();
  }

  private void calculatePermissions() {
    Set<String> extraPermissions = new HashSet<>();
    for (String permission : permissions) {
      int index = getPermissionIndex(permission);
      extraPermissions.add(permission.substring(0, index) + READ_PERMISSION);
      if (permission.substring(index).equals(ALL_PERMISSION)) {
        allPrefixes.add(permission.substring(0, index));
      }
    }
    permissions.addAll(extraPermissions);
  }

  protected int getPermissionIndex(String permission) {
    return permission.lastIndexOf(PERMISSION_PREFIX_DELIMITER) + 1;
  }

  public boolean hasPermission(String permission) {
    String prefix = permission.substring(0, getPermissionIndex(permission));
    return allPrefixes.contains(prefix) || permissions.contains(permission);
  }

}
