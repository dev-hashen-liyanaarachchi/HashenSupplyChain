package com.globaltrade.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.ShipmentStatus;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ShipmentRepository {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void save(Shipment shipment) {
        em.persist(shipment);
    }

    public Shipment update(Shipment shipment) {
        return em.merge(shipment);
    }

    public Optional<Shipment> findById(Long id) {
        return Optional.ofNullable(em.find(Shipment.class, id));
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return em.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :tNo", Shipment.class)
                .setParameter("tNo", trackingNumber)
                .getResultStream()
                .findFirst();
    }

    public List<Shipment> findByStatus(ShipmentStatus status) {
        return em.createQuery("SELECT s FROM Shipment s WHERE s.status = :status", Shipment.class)
                .setParameter("status", status)
                .getResultList();
    }
}
