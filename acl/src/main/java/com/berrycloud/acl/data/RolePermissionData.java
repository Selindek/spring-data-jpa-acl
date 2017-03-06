package com.berrycloud.acl.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RolePermissionData extends PermissionData {

    private Set<String> roleNames;

    public RolePermissionData(String[] roleNames, String[] permissions) {
        super(permissions);
        this.roleNames = new HashSet<String>(Arrays.asList(roleNames));
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

}
