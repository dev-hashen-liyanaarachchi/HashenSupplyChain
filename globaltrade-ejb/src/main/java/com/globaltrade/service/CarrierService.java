package com.globaltrade.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Carrier;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.ShipmentType;
import com.globaltrade.enums.ShipmentStatus;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class CarrierService {

    private static final Logger LOGGER = Logger.getLogger(CarrierService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private NotificationService notificationService;

    @EJB
    private TrackingEventService trackingEventService;

    public List<Carrier> getAllCarriers() {
        List<Carrier> list = em.createQuery("SELECT c FROM Carrier c ORDER BY c.id ASC", Carrier.class).getResultList();
        if (list.isEmpty()) {
            LOGGER.info("[CARRIER SEED] Seeding initial destination country local carrier fleet network...");

            Carrier c1 = new Carrier("Sri Lanka Logistics Express Ground Fleet", ShipmentType.EXPRESS_COURIER, "LK");
            c1.setContactPhone("+94 11 234 5678");
            c1.setContactEmail("dispatch@srilankalogistics.lk");
            c1.setFleetSize("45 Express Delivery Trucks & Vans");
            c1.setOperatingStatus("ACTIVE");
            em.persist(c1);

            Carrier c2 = new Carrier("Colombo Air Cargo Terminal Agent", ShipmentType.AIR_FREIGHT, "LK");
            c2.setContactPhone("+94 11 456 7890");
            c2.setContactEmail("aircargo@colombo-terminal.lk");
            c2.setFleetSize("12 Air Cargo Warehouses & Vans");
            c2.setOperatingStatus("ACTIVE");
            em.persist(c2);

            Carrier c3 = new Carrier("DHL Express Deutschland Air Fleet", ShipmentType.AIR_FREIGHT, "DE");
            c3.setContactPhone("+49 69 123456");
            c3.setContactEmail("dispatch@dhl.de");
            c3.setFleetSize("120 International Cargo Aircraft");
            c3.setOperatingStatus("ACTIVE");
            em.persist(c3);

            Carrier c4 = new Carrier("FedEx Trade Networks North America", ShipmentType.AIR_FREIGHT, "US");
            c4.setContactPhone("+1 800 463 3339");
            c4.setContactEmail("customs@fedex.com");
            c4.setFleetSize("350 Express Aircraft & Vans");
            c4.setOperatingStatus("ACTIVE");
            em.persist(c4);

            Carrier c5 = new Carrier("Singapore Changi Express Cargo Hub", ShipmentType.SEA_FREIGHT, "SG");
            c5.setContactPhone("+65 6123 4567");
            c5.setContactEmail("logistics@changi.sg");
            c5.setFleetSize("80 Feeder Vessels & Trucks");
            c5.setOperatingStatus("ACTIVE");
            em.persist(c5);

            list = em.createQuery("SELECT c FROM Carrier c ORDER BY c.id ASC", Carrier.class).getResultList();
        }
        return list;
    }

    public Carrier addCarrier(Carrier carrier) {
        if (carrier.getCountryCode() == null || carrier.getCountryCode().isBlank()) {
            carrier.setCountryCode("LK");
        }
        if (carrier.getOperatingStatus() == null || carrier.getOperatingStatus().isBlank()) {
            carrier.setOperatingStatus("ACTIVE");
        }
        em.persist(carrier);
        LOGGER.info("[CARRIER CREATED] Registered new carrier: " + carrier.getName() + " (" + carrier.getCountryCode() + ")");
        return carrier;
    }

    public Shipment handoverCargoToDestinationCarrier(Long shipmentId, Long carrierId, String driverName, String vehicleNo) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found: #" + shipmentId);
        }

        Carrier carrier = em.find(Carrier.class, carrierId);
        if (carrier == null) {
            carrier = getAllCarriers().get(0);
        }

        String driverInfo = (driverName != null && !driverName.isBlank()) ? driverName : "Agent K. Perera";
        String vehicleInfo = (vehicleNo != null && !vehicleNo.isBlank()) ? vehicleNo : "WP-BC-8910";

        shipment.setCarrier(carrier);
        shipment.setDriverName(driverInfo);
        shipment.setVehicleNo(vehicleInfo);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        em.merge(shipment);

        if (trackingEventService != null) {
            trackingEventService.recordTrackingEvent(shipment, "Destination Port (" + carrier.getCountryCode() + ")", "Cargo received & handed over to " + carrier.getName() + " (Driver: " + driverInfo + ", Vehicle: " + vehicleInfo + ").");
        }

        if (notificationService != null) {
            notificationService.createNotification(
                    "🚚 Destination Carrier Handover Completed",
                    "Cargo #" + shipment.getTrackingNumber() + " received at destination port (" + carrier.getCountryCode() + ") and handed over to " + carrier.getName() + " (Driver: " + driverInfo + ", Vehicle: " + vehicleInfo + ") for final customer delivery!",
                    "LOGISTICS",
                    "SUCCESS"
            );
        }

        LOGGER.info("[CARRIER HANDOVER SUCCESS] Shipment #" + shipment.getTrackingNumber() + " assigned to " + carrier.getName());
        return shipment;
    }
}
