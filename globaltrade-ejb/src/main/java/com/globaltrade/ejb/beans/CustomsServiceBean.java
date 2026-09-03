package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.CustomsService;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.CustomsDocumentStatus;
import com.globaltrade.exception.CustomsException;
import com.globaltrade.exception.TariffComplianceException;
import com.globaltrade.exception.ResourceNotFoundException;

import java.util.logging.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class CustomsServiceBean implements CustomsService {

    private static final Logger LOGGER = Logger.getLogger(CustomsServiceBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    // CMT REQUIRES_NEW: Creates independent transaction that commits even if caller transaction rolls back
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public CustomsDocument fileCustomsDeclaration(Long shipmentId, String documentType, String hsCode) throws CustomsException {
        LOGGER.info("[CMT REQUIRES_NEW] Filing independent Customs Document for Shipment ID: " + shipmentId);

        if (hsCode == null || hsCode.isBlank() || hsCode.startsWith("9999") || hsCode.startsWith("9998")) {
            throw new TariffComplianceException(hsCode != null ? hsCode : "MISSING", "Blacklisted or non-compliant Harmonized System (HS) Tariff Code detected.");
        }

        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new ResourceNotFoundException("Shipment", shipmentId);
        }

        CustomsDocument doc = new CustomsDocument(shipment, documentType, hsCode);
        em.persist(doc);
        LOGGER.info("[CMT REQUIRES_NEW SUCCESS] Customs Document ID: " + doc.getId() + " filed independently!");
        return doc;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CustomsDocument inspectAndApprove(Long documentId, String inspectorName) throws CustomsException {
        CustomsDocument doc = em.find(CustomsDocument.class, documentId);
        if (doc == null) {
            throw new ResourceNotFoundException("CustomsDocument", documentId);
        }

        doc.setStatus(CustomsDocumentStatus.APPROVED);
        doc.setInspectedBy(inspectorName != null ? inspectorName : "INSPECTOR_GENERAL");
        return em.merge(doc);
    }
}
