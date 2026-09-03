package com.globaltrade.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Notification;
import com.globaltrade.entity.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public Notification createNotification(String title, String message, String category, String severity) {
        LOGGER.info("[NOTIFICATION SERVICE] Creating System Notification: [" + category + "] " + title + " - " + message);
        
        List<User> users = em.createQuery("SELECT u FROM User u", User.class).setMaxResults(1).getResultList();
        User recipient = users.isEmpty() ? null : users.get(0);

        if (recipient == null) {
            List<com.globaltrade.entity.Role> roles = em.createQuery("SELECT r FROM com.globaltrade.entity.Role r", com.globaltrade.entity.Role.class).setMaxResults(1).getResultList();
            com.globaltrade.entity.Role r = roles.isEmpty() ? null : roles.get(0);
            if (r == null) {
                r = new com.globaltrade.entity.Role("ADMIN");
                em.persist(r);
            }
            recipient = new User("admin", "hash", "admin@globaltrade.com", r);
            em.persist(recipient);
        }

        Notification notif = new Notification(recipient, "[" + category + "] " + title, message);
        em.persist(notif);
        return notif;
    }

    public List<Notification> getAllNotifications() {
        List<Notification> list = em.createQuery("SELECT n FROM Notification n ORDER BY n.id DESC", Notification.class)
                .setMaxResults(30)
                .getResultList();

        if (list.isEmpty()) {
            // Seed initial notifications if table is empty
            createNotification("Customs Clearance Required", "Shipment #TRK-DHL-91823 requires 48-hr tariff verification for Sri Lanka import.", "CUSTOMS", "WARNING");
            createNotification("Low Stock Alert: Siemens Ultrasound", "USA Depot stock reached threshold (15 units). Supplier reorder PO created.", "INVENTORY", "CRITICAL");
            createNotification("Cargo Transit Advanced", "Shipment #TRK-DHL-91823 departed Dubai Air Freight Hub.", "LOGISTICS", "SUCCESS");
            list = em.createQuery("SELECT n FROM Notification n ORDER BY n.id DESC", Notification.class).getResultList();
        }
        return list;
    }

    public void markAsRead(Long notificationId) {
        Notification notif = em.find(Notification.class, notificationId);
        if (notif != null) {
            notif.setReadStatus(true);
            em.merge(notif);
        }
    }

    public void clearAll() {
        em.createQuery("DELETE FROM Notification n").executeUpdate();
    }
}
