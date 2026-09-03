package com.globaltrade.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.CustomsDocumentStatus;
import com.globaltrade.enums.ShipmentStatus;
import com.globaltrade.interceptor.AuditInterceptor;
import com.globaltrade.interceptor.ComplianceInterceptor;
import com.globaltrade.interceptor.PerformanceInterceptor;

import java.util.List;
import java.util.Map;

@Stateless
@Interceptors({AuditInterceptor.class, PerformanceInterceptor.class, ComplianceInterceptor.class})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CustomsService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @jakarta.ejb.EJB
    private TrackingEventService trackingEventService;

    public CustomsDocument generateCustomsDeclaration(Long shipmentId, String hsCode, String docType) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found with ID: " + shipmentId);
        }

        String type = (docType != null && !docType.isBlank()) ? docType : "COMMERCIAL_INVOICE_PACKING_LIST";
        String tariff = (hsCode != null && !hsCode.isBlank()) ? hsCode : "8471.30";

        CustomsDocument doc = new CustomsDocument(shipment, type, tariff);
        doc.setStatus(CustomsDocumentStatus.SUBMITTED);
        em.persist(doc);

        shipment.setStatus(ShipmentStatus.IN_CUSTOMS);
        em.merge(shipment);

        return doc;
    }

    @RolesAllowed({"CUSTOMS_OFFICER", "ADMIN"})
    public CustomsDocument reviewCustomsDocument(Long docId, String officerName, boolean approve, String notes) {
        CustomsDocument doc = em.find(CustomsDocument.class, docId);
        if (doc == null) {
            throw new IllegalArgumentException("Customs Document not found with ID: " + docId);
        }

        doc.setInspectedBy(officerName != null ? officerName : "Customs Inspector");
        if (approve) {
            doc.setStatus(CustomsDocumentStatus.APPROVED);
            if (doc.getShipment() != null) {
                doc.getShipment().setStatus(ShipmentStatus.CUSTOMS_CLEARED);
                em.merge(doc.getShipment());
                if (trackingEventService != null) {
                    trackingEventService.recordTrackingEvent(doc.getShipment(), "Government Customs Office", "Customs inspection passed. Document #" + doc.getId() + " approved by Inspector " + doc.getInspectedBy() + ".");
                }
            }
        } else {
            doc.setStatus(CustomsDocumentStatus.REJECTED);
            if (doc.getShipment() != null) {
                doc.getShipment().setStatus(ShipmentStatus.CUSTOMS_HOLD);
                em.merge(doc.getShipment());
                if (trackingEventService != null) {
                    trackingEventService.recordTrackingEvent(doc.getShipment(), "Government Customs Office", "Customs inspection placed on hold. Document #" + doc.getId() + " flagged for tariff review.");
                }
            }
        }

        em.merge(doc);
        return doc;
    }

    public List<CustomsDocument> getAllCustomsDocuments() {
        List<CustomsDocument> list = em.createQuery("SELECT DISTINCT d FROM CustomsDocument d LEFT JOIN FETCH d.shipment s LEFT JOIN FETCH s.order LEFT JOIN FETCH s.originWarehouse w ORDER BY d.id DESC", CustomsDocument.class)
                .getResultList();

        if (list.isEmpty()) {
            List<Shipment> shipments = em.createQuery("SELECT s FROM Shipment s LEFT JOIN FETCH s.order LEFT JOIN FETCH s.originWarehouse", Shipment.class).getResultList();
            for (Shipment s : shipments) {
                CustomsDocument doc = new CustomsDocument(s, "COMMERCIAL_INVOICE_PACKING_LIST", "9018.90");
                doc.setStatus(CustomsDocumentStatus.SUBMITTED);
                doc.setInspectedBy("Pending Customs Inspection");
                doc.setDeclaredValue(s.getOrder() != null ? s.getOrder().getItemsSubtotal() : 4890.00);
                doc.setDutyFee(s.getOrder() != null ? Math.round(s.getOrder().getItemsSubtotal() * 0.05 * 100.0) / 100.0 : 244.50);
                doc.setOriginCountry(s.getOriginWarehouse() != null && s.getOriginWarehouse().getAddress() != null && s.getOriginWarehouse().getAddress().getCountry() != null ? s.getOriginWarehouse().getAddress().getCountry().getCode() : "US");
                doc.setDestinationCountry(s.getDestinationAddress() != null && s.getDestinationAddress().getCountry() != null ? s.getDestinationAddress().getCountry().getCode() : "LK");
                doc.setExporterName(s.getOriginWarehouse() != null ? s.getOriginWarehouse().getName() : "USA New York Air Cargo Center");
                doc.setImporterName(s.getOrder() != null && s.getOrder().getCustomer() != null ? (s.getOrder().getCustomer().getFirstName() + " " + s.getOrder().getCustomer().getLastName()) : "International Medical Imports");
                doc.setPackingListItems("1x Siemens Diagnostic Ultrasound Transducer [HS 9018.90], 1x Export Air Container");
                doc.setClearanceDeadline(java.time.LocalDateTime.now().plusHours(48));
                doc.setSettlementStatus("PENDING_DUTY_SETTLEMENT");
                doc.setAssignedCarrier(null);
                em.persist(doc);
            }
            list = em.createQuery("SELECT DISTINCT d FROM CustomsDocument d LEFT JOIN FETCH d.shipment s LEFT JOIN FETCH s.order LEFT JOIN FETCH s.originWarehouse w ORDER BY d.id DESC", CustomsDocument.class).getResultList();
        }
        return list;
    }
}
