package com.berrycloud.acl.data;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

public class RolePermissionData extends PermissionData {

    private Set<GrantedAuthority> authorities;

    public RolePermissionData(Set<GrantedAuthority> authorities, String[] permissions) {
        super(permissions);
        this.authorities = authorities;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

}
