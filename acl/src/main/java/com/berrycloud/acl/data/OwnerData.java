package com.berrycloud.acl.data;

public class OwnerData extends PropertyPermissionData {

	private boolean collection;
	private Class<?> propertyType;

	public OwnerData(String propertyName, Class<?> propertyType, boolean collection, String[] permissions) {
		super(propertyName, permissions);
		this.collection = collection;
		this.propertyType = propertyType;
	}

	public boolean isCollection() {
		return collection;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}

}
