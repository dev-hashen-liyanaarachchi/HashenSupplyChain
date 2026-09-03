package com.globaltrade.timer;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.util.logging.Logger;

@Singleton
@Startup
public class RefreshTokenCleanupScheduler {

    private static final Logger LOGGER =
            Logger.getLogger(RefreshTokenCleanupScheduler.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Schedule(hour = "23", minute = "59", persistent = false)
    public void scheduleCleanup() {
        int deleted = em.createNamedQuery("RefreshToken.deleteExpiredToken")
                .setParameter("now", Instant.now())
                .executeUpdate();

        LOGGER.info("Refresh token cleanup : " + deleted + " tokens deleted.");
    }
}
