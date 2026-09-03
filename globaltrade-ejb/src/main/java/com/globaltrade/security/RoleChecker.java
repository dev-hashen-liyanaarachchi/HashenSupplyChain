package com.globaltrade.security;

import jakarta.enterprise.context.ApplicationScoped;
import com.globaltrade.entity.Role;

import java.util.Set;

@ApplicationScoped
public class RoleChecker {

    public boolean hasRole(Role userRole, String targetRoleName) {
        return userRole != null && userRole.getName().equalsIgnoreCase(targetRoleName);
    }

    public boolean hasPermission(Role userRole, String permissionName) {
        if (userRole == null || userRole.getPermissions() == null) return false;
        Set<String> permissions = userRole.getPermissions();
        return permissions.stream().anyMatch(p -> p.equalsIgnoreCase(permissionName));
    }
}
