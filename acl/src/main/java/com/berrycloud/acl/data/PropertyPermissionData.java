package com.berrycloud.acl.data;

public class PropertyPermissionData extends PermissionData {

	private String propertyName;

	public PropertyPermissionData(String propertyName, String[] permissions) {
		super(permissions);
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

}
