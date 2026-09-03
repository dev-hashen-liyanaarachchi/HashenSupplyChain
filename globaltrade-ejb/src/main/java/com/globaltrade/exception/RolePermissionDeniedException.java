package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class RolePermissionDeniedException extends GlobalTradeException {

    private final String requiredRole;
    private final String currentRole;

    public RolePermissionDeniedException(String requiredRole, String currentRole) {
        super("Access Denied: Required Role [" + requiredRole + "] but Current User has Role [" + currentRole + "]");
        this.requiredRole = requiredRole;
        this.currentRole = currentRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public String getCurrentRole() {
        return currentRole;
    }
}
