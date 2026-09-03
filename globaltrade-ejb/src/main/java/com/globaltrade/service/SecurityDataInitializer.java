package com.globaltrade.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.logging.Logger;

@Singleton
@Startup
public class SecurityDataInitializer {

    private static final Logger LOGGER = Logger.getLogger(SecurityDataInitializer.class.getName());

    @EJB
    private SecurityService securityService;

    @PostConstruct
    public void init() {
        LOGGER.info("[SECURITY STARTUP INITIALIZER] Automatically seeding roles, permissions, and role_permissions database tables on server deployment...");
        try {
            if (securityService != null) {
                securityService.getAllPermissions();
                securityService.getAllRoles();
                LOGGER.info("[SECURITY STARTUP INITIALIZER SUCCESS] Database tables (roles, permissions, role_permissions) populated successfully!");
            }
        } catch (Exception e) {
            LOGGER.severe("[SECURITY STARTUP ERROR] Failed to seed security tables: " + e.getMessage());
        }
    }
}
