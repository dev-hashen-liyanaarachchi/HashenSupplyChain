package com.globaltrade.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.enums.CustomsDocumentStatus;
import com.globaltrade.enums.ShipmentStatus;
import com.globaltrade.entity.Shipment;

import java.util.List;
import java.util.logging.Logger;

import com.globaltrade.entity.Order;
import com.globaltrade.entity.Payment;
import com.globaltrade.enums.PaymentMethod;
import com.globaltrade.enums.PaymentStatus;

@Stateless
public class FinanceService {

    private static final Logger LOGGER = Logger.getLogger(FinanceService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private CustomsService customsService;

    @EJB
    private NotificationService notificationService;

    @EJB
    private TrackingEventService trackingEventService;

    public List<CustomsDocument> getClearedCustomsForSettlement() {
        return customsService.getAllCustomsDocuments();
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = em.createQuery("SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.order o LEFT JOIN FETCH o.customer ORDER BY p.id DESC", Payment.class).getResultList();
        if (list.isEmpty()) {
            LOGGER.info("[PAYMENT SEED] Auto-seeding initial payment records into payments database table...");
            List<Order> orders = em.createQuery("SELECT o FROM Order o LEFT JOIN FETCH o.customer", Order.class).getResultList();
            for (Order o : orders) {
                String txnRef = "TXN-GT-" + (100000 + o.getId() * 17) + "-VISA";
                Payment p = new Payment(o, txnRef, PaymentMethod.CREDIT_CARD, o.getTotalAmount() != null ? o.getTotalAmount() : 4890.00);
                p.setPaymentStatus(PaymentStatus.COMPLETED);
                em.persist(p);
            }
            list = em.createQuery("SELECT DISTINCT p FROM Payment p LEFT JOIN FETCH p.order o LEFT JOIN FETCH o.customer ORDER BY p.id DESC", Payment.class).getResultList();
        }
        return list;
    }

    public CustomsDocument settleDutyAndHandoverToCarrier(Long documentId, String carrierName, String financeOfficer) {
        CustomsDocument doc = em.find(CustomsDocument.class, documentId);
        if (doc == null) {
            throw new IllegalArgumentException("Customs Document not found: #" + documentId);
        }

        LOGGER.info("[FINANCE DUTY SETTLEMENT] Settling import duty tax $" + doc.getDutyFee() + " for Doc #" + documentId + " by " + financeOfficer);

        doc.setSettlementStatus("DUTY_SETTLED_AND_DISPATCHED");
        doc.setAssignedCarrier(carrierName != null ? carrierName : "DHL Express International Air Fleet");
        doc.setStatus(CustomsDocumentStatus.APPROVED);

        Shipment shipment = doc.getShipment();
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.IN_TRANSIT);
            em.merge(shipment);
            if (trackingEventService != null) {
                trackingEventService.recordTrackingEvent(shipment, "Finance Settlement Center", "Import tariff duty ($" + doc.getDutyFee() + ") settled. Cargo consolidated for dispatch via " + doc.getAssignedCarrier() + ".");
            }
        }

        em.merge(doc);

        if (notificationService != null) {
            notificationService.createNotification(
                    "💳 Import Duty Settled & Carrier Handover",
                    "Customs Clearance Document #DOC-" + documentId + " duty tax ($" + doc.getDutyFee() + ") settled. Cargo consolidated & handed over to " + doc.getAssignedCarrier() + " for final customer delivery!",
                    "CUSTOMS",
                    "SUCCESS"
            );
        }

        return doc;
    }
}
