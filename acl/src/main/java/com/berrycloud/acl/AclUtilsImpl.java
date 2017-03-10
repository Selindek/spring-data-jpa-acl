package com.berrycloud.acl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.berrycloud.acl.security.AclUserDetails;
import com.berrycloud.acl.security.AclUserDetailsService;

public class AclUtilsImpl implements AclUtils {

    @Autowired
    AclUserDetailsService<?> aclUserDetailsService;

    @Override
    public Object getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public AclUserDetails getAclUserDetails() {
        Object principal = getPrincipal();
        if (principal instanceof AclUserDetails) {
            return (AclUserDetails) principal;
        }
        return null;
    }

    @Override
    public UserDetails getUserDetails() {
        Object principal = getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    @Override
    public String getUsername() {
        Object principal = getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal != null) {
            return principal.toString();
        }
        return null;
    }

    @Override
    public boolean isAdmin() {
        return hasAuthority(AclConstants.ROLE_ADMIN);
    }

    @Override
    public boolean hasAuthority(String authority) {
        UserDetails currentUser = getUserDetails();
        return currentUser != null
                && currentUser.getAuthorities().contains(aclUserDetailsService.createGrantedAuthority(authority));
    }

    @Override
    public boolean hasAnyAuthorities(Set<GrantedAuthority> authorities) {
        if (authorities.isEmpty()) {
            return true;
        }
        UserDetails currentUser = getUserDetails();
        if (currentUser != null) {

            for (GrantedAuthority authority : authorities) {
                if (currentUser.getAuthorities().contains(authority)) {
                    return true;
                }
            }
        }
        return false;
    }

}
