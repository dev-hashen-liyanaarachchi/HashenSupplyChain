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

        if (hsCode == null || hsCode.startsWith("9999")) {
            throw new CustomsException(hsCode, "Blacklisted HS Code detected during trade compliance check.");
        }

        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found with ID: " + shipmentId);
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
            throw new CustomsException("UNKNOWN", "Customs Document not found.");
        }

        doc.setStatus(CustomsDocumentStatus.APPROVED);
        doc.setInspectedBy(inspectorName);
        return em.merge(doc);
    }
}
