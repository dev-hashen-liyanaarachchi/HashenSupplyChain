package com.globaltrade.security;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;

import java.security.Principal;
import java.util.logging.Logger;

@Stateless
@DeclareRoles({"LOGISTICS_COORDINATOR", "CUSTOMS_AGENT", "WAREHOUSE_MANAGER", "VENDOR_REP", "SYSTEM_ADMIN"})
public class AuthorizationService {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationService.class.getName());

    @Resource
    private SessionContext sessionContext;

    public Principal getCurrentPrincipal() {
        return sessionContext != null ? sessionContext.getCallerPrincipal() : null;
    }

    public boolean isCallerInRole(String roleName) {
        if (sessionContext == null) return false;
        boolean inRole = sessionContext.isCallerInRole(roleName);
        LOGGER.info("[PROGRAMMATIC AUTHORIZATION CHECK] Principal: " + sessionContext.getCallerPrincipal().getName() + " Role: " + roleName + " -> " + inRole);
        return inRole;
    }

    public void checkPermission(String requiredRole) throws SecurityException {
        if (!isCallerInRole(requiredRole)) {
            String caller = sessionContext != null && sessionContext.getCallerPrincipal() != null
                    ? sessionContext.getCallerPrincipal().getName() : "ANONYMOUS";
            throw new SecurityException("Access Denied: Caller [" + caller + "] does not possess required role [" + requiredRole + "]");
        }
    }
}
