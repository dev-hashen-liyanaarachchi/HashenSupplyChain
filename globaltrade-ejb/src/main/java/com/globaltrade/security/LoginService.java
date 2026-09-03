package com.globaltrade.security;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.User;
import com.globaltrade.entity.Role;
import com.globaltrade.entity.Vendor;
import com.globaltrade.enums.VendorStatus;
import com.globaltrade.exception.UserAlreadyExistsException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Stateless
public class LoginService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public Optional<User> findByEmail(String email) {
        try {
            return Optional.of(em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            return Optional.of(em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        Optional<User> byUsername = findByUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return findByEmail(identifier);
    }

    public boolean validate(String identifier, String password) {
        return findByUsernameOrEmail(identifier)
                .map(user -> user.getPasswordHash().equals(password))
                .orElse(false);
    }

    public Set<String> getRoles(String identifier) {
        return findByUsernameOrEmail(identifier)
                .map(user -> Set.of(user.getRole().getName()))
                .orElse(Collections.emptySet());
    }

    public User registerUser(String username, String email, String password, String roleName) {
        if (findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }

        String targetRole = (roleName != null && !roleName.isBlank()) ? roleName : "CUSTOMER";
        Role role = em.createQuery("SELECT r FROM Role r WHERE r.name = :rname", Role.class)
                .setParameter("rname", targetRole)
                .getResultStream().findFirst()
                .orElseGet(() -> {
                    Role r = new Role(targetRole);
                    em.persist(r);
                    return r;
                });

        User user = new User(username, password, email, role);
        em.persist(user);

        // Auto create Vendor record if registering as VENDOR or VENDOR_REP
        if ("VENDOR".equalsIgnoreCase(targetRole) || "VENDOR_REP".equalsIgnoreCase(targetRole)) {
            Vendor vendor = new Vendor(user, username + " Enterprise", "TIN-" + System.currentTimeMillis(), null);
            vendor.setStatus(VendorStatus.ACTIVE);
            em.persist(vendor);
        }

        return user;
    }
}
