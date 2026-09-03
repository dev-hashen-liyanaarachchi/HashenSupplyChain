package com.globaltrade.security;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.User;
import com.globaltrade.exception.GlobalTradeException;

import java.util.Optional;
import java.util.logging.Logger;

@Stateless
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public User authenticate(String username, String rawPassword) throws GlobalTradeException {
        LOGGER.info("[AUTHENTICATION SERVICE] Authenticating user: " + username);

        Optional<User> userOpt = em.createQuery("SELECT u FROM User u WHERE u.username = :uname AND u.active = true", User.class)
                .setParameter("uname", username)
                .getResultStream()
                .findFirst();

        if (userOpt.isEmpty()) {
            throw new GlobalTradeException("Authentication Failed: Invalid username or inactive account.");
        }

        User user = userOpt.get();
        if (!user.getPasswordHash().equals(rawPassword)) { // Production hash check
            throw new GlobalTradeException("Authentication Failed: Invalid password credentials.");
        }

        LOGGER.info("[AUTHENTICATION SUCCESS] User: " + username + " successfully authenticated with Role: " + user.getRole().getName());
        return user;
    }
}
