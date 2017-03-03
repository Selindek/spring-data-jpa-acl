package com.berrycloud.acl.data;

public class ParentData extends PropertyPermissionData {

	private String permissionPrefix;

	public ParentData(String propertyName, String permissionPrefix, String[] permissions) {
		super(propertyName, permissions);
		this.permissionPrefix = permissionPrefix;
	}

	public String getPermissionPrefix() {
		return permissionPrefix;
	}

}
