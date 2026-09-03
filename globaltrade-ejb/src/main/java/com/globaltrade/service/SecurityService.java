package com.globaltrade.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Stateless
public class SecurityService {

    private static final Logger LOGGER = Logger.getLogger(SecurityService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public List<Map<String, String>> getAllPermissions() {
        return List.of(
                Map.of("name", "MANAGE_USERS", "description", "Full administrative user & RBAC management"),
                Map.of("name", "VIEW_CUSTOMS", "description", "View international customs declarations & tariff details"),
                Map.of("name", "APPROVE_CUSTOMS", "description", "Inspect and approve/reject customs export/import clearance"),
                Map.of("name", "SETTLE_DUTY", "description", "Settle import duty tariffs & execute financial transactions"),
                Map.of("name", "DISPATCH_CARRIER", "description", "Authorize carrier flight departures & cargo handover"),
                Map.of("name", "MANAGE_CARRIERS", "description", "Register partner logistics carriers & local driver fleets"),
                Map.of("name", "VIEW_ANALYTICS", "description", "Access vendor SLA performance & system timer dashboards"),
                Map.of("name", "PLACE_ORDER", "description", "Storefront customer purchasing & end-to-end cargo tracking")
        );
    }

    public List<Role> getAllRoles() {
        List<Role> list = em.createQuery("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions ORDER BY r.id ASC", Role.class).getResultList();
        if (list.isEmpty()) {
            LOGGER.info("[SECURITY SEED] Seeding roles and role_permissions database tables...");

            Role rAdmin = new Role("ADMIN");
            rAdmin.getPermissions().addAll(List.of("MANAGE_USERS", "VIEW_CUSTOMS", "APPROVE_CUSTOMS", "SETTLE_DUTY", "DISPATCH_CARRIER", "MANAGE_CARRIERS", "VIEW_ANALYTICS", "PLACE_ORDER"));
            em.persist(rAdmin);

            Role rCustoms = new Role("CUSTOMS_OFFICER");
            rCustoms.getPermissions().addAll(List.of("VIEW_CUSTOMS", "APPROVE_CUSTOMS"));
            em.persist(rCustoms);

            Role rFinance = new Role("FINANCE_OFFICER");
            rFinance.getPermissions().addAll(List.of("SETTLE_DUTY", "VIEW_ANALYTICS"));
            em.persist(rFinance);

            Role rDispatcher = new Role("CARRIER_DISPATCHER");
            rDispatcher.getPermissions().addAll(List.of("DISPATCH_CARRIER", "MANAGE_CARRIERS"));
            em.persist(rDispatcher);

            Role rLogistics = new Role("LOGISTICS_MANAGER");
            rLogistics.getPermissions().addAll(List.of("VIEW_CUSTOMS", "DISPATCH_CARRIER", "MANAGE_CARRIERS", "VIEW_ANALYTICS"));
            em.persist(rLogistics);

            Role rCustomer = new Role("CUSTOMER");
            rCustomer.getPermissions().addAll(List.of("PLACE_ORDER"));
            em.persist(rCustomer);

            list = em.createQuery("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions ORDER BY r.id ASC", Role.class).getResultList();
        }
        return list;
    }

    @RolesAllowed("ADMIN")
    public Role assignPermissionsToRole(Long roleId, List<String> permNames) {
        Role role = em.find(Role.class, roleId);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: #" + roleId);
        }

        Set<String> updatedPerms = new HashSet<>();
        if (permNames != null && !permNames.isEmpty()) {
            updatedPerms.addAll(permNames);
        }

        role.setPermissions(updatedPerms);
        em.merge(role);

        LOGGER.info("[RBAC UPDATED] Assigned " + updatedPerms.size() + " permissions to Role: " + role.getName() + " in role_permissions table!");
        return role;
    }
}
