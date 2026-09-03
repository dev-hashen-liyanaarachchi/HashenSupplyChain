package com.globaltrade.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.User;
import com.globaltrade.entity.Role;
import java.util.*;

@Stateless
public class AdminManagementService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public List<Map<String, Object>> getAllAdminUsers() {
        List<User> users = em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.role ORDER BY u.id ASC", User.class).getResultList();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("role", u.getRole() != null ? u.getRole().getName() : "ADMIN");
            map.put("active", u.isActive());
            map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "2026-08-24");
            result.add(map);
        }

        return result;
    }

    public Map<String, Object> createAdminUser(String username, String password, String email, Long roleId) {
        if (username == null || username.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Username and email are required for Admin creation.");
        }

        Role role = em.find(Role.class, roleId != null ? roleId : 1L);
        if (role == null) {
            role = em.createQuery("SELECT r FROM Role r WHERE r.name = 'ADMIN'", Role.class).getResultStream().findFirst().orElse(null);
            if (role == null) {
                role = new Role("ADMIN");
                em.persist(role);
            }
        }

        String pwd = (password != null && !password.isBlank()) ? password : "admin123";
        User newUser = new User(username, pwd, email, role);
        newUser.setActive(true);

        em.persist(newUser);

        Map<String, Object> map = new HashMap<>();
        map.put("id", newUser.getId());
        map.put("username", newUser.getUsername());
        map.put("email", newUser.getEmail());
        map.put("role", role.getName());
        map.put("active", true);
        map.put("createdAt", newUser.getCreatedAt() != null ? newUser.getCreatedAt().toString() : "2026-08-24");

        return map;
    }
}
