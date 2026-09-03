package com.globaltrade.ejb.beans;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import com.globaltrade.ejb.interfaces.CustomsService;
import com.globaltrade.ejb.interfaces.ShipmentService;
import com.globaltrade.entity.Address;
import com.globaltrade.entity.Carrier;
import com.globaltrade.entity.Order;
import com.globaltrade.entity.Shipment;
import com.globaltrade.entity.Warehouse;
import com.globaltrade.dto.ShipmentDTO;
import com.globaltrade.enums.ShipmentStatus;
import com.globaltrade.exception.CustomsException;
import com.globaltrade.exception.ShipmentException;
import com.globaltrade.repository.ShipmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ShipmentServiceBean implements ShipmentService {

    private static final Logger LOGGER = Logger.getLogger(ShipmentServiceBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private ShipmentRepository shipmentRepository;

    @EJB
    private CustomsService customsService;

    @Resource
    private UserTransaction userTransaction;

    @Override
    public Shipment createShipment(ShipmentDTO dto) throws ShipmentException, CustomsException {
        try {
            userTransaction.begin();

            Order order = em.find(Order.class, dto.getOrderId());
            Warehouse origin = em.find(Warehouse.class, dto.getOriginWarehouseId());
            Address destination = em.find(Address.class, dto.getDestinationAddressId());

            if (order == null || origin == null || destination == null) {
                userTransaction.rollback();
                throw new ShipmentException("INVALID", "Invalid Order, Warehouse, or Address ID.");
            }

            String trackingNo = "GTX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Shipment shipment = new Shipment(trackingNo, order, dto.getShipmentType(), origin, destination, LocalDateTime.now().plusDays(7));

            if (dto.getCarrierId() != null) {
                Carrier carrier = em.find(Carrier.class, dto.getCarrierId());
                shipment.setCarrier(carrier);
            }

            shipmentRepository.save(shipment);
            userTransaction.commit();

            // File independent customs document (CMT REQUIRES_NEW)
            customsService.fileCustomsDeclaration(shipment.getId(), "EXPORT_DECLARATION", dto.getHsCode());

            return shipment;
        } catch (Exception ex) {
            try {
                if (userTransaction.getStatus() == jakarta.transaction.Status.STATUS_ACTIVE) {
                    userTransaction.rollback();
                }
            } catch (Exception e) {
                LOGGER.severe("Rollback failure: " + e.getMessage());
            }
            if (ex instanceof CustomsException) throw (CustomsException) ex;
            if (ex instanceof ShipmentException) throw (ShipmentException) ex;
            throw new ShipmentException("CREATION_FAILED", ex.getMessage());
        }
    }

    // BMT: Bean-Managed Transaction batch manifest processing
    @Override
    public int processBatchShipmentUpdate(List<Long> shipmentIds, ShipmentStatus status) {
        LOGGER.info("[BMT STARTED] Updating status for shipment batch size: " + shipmentIds.size());
        int count = 0;

        for (Long id : shipmentIds) {
            try {
                userTransaction.begin();
                Shipment shipment = em.find(Shipment.class, id);
                if (shipment != null) {
                    shipment.setStatus(status);
                    em.merge(shipment);
                    userTransaction.commit();
                    count++;
                } else {
                    userTransaction.rollback();
                }
            } catch (Exception e) {
                try {
                    userTransaction.rollback();
                } catch (Exception rollbackEx) {
                }
                LOGGER.severe("[BMT BATCH FAILURE] Shipment ID " + id + " failed in batch: " + e.getMessage());
            }
        }
        return count;
    }

    @Override
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber).orElse(null);
    }
}
