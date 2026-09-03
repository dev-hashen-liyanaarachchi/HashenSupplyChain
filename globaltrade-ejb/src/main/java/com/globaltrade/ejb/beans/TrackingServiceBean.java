package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.TrackingService;
import com.globaltrade.entity.Shipment;
import com.globaltrade.entity.TrackingEvent;
import com.globaltrade.repository.ShipmentRepository;
import java.util.List;

@Stateless
public class TrackingServiceBean implements TrackingService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private ShipmentRepository shipmentRepository;

    @Override
    public TrackingEvent addTrackingEvent(String trackingNumber, String location, String description) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new IllegalArgumentException("Shipment not found with tracking #" + trackingNumber));

        TrackingEvent event = new TrackingEvent(shipment, location, description);
        em.persist(event);
        return event;
    }

    @Override
    public List<TrackingEvent> getTrackingHistory(String trackingNumber) {
        return em.createQuery("SELECT t FROM TrackingEvent t WHERE t.shipment.trackingNumber = :tNo ORDER BY t.timestamp DESC", TrackingEvent.class)
                .setParameter("tNo", trackingNumber)
                .getResultList();
    }
}
