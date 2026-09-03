package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Shipment;
import com.globaltrade.dto.ShipmentDTO;
import com.globaltrade.enums.ShipmentStatus;
import com.globaltrade.exception.CustomsException;
import com.globaltrade.exception.ShipmentException;

import java.util.List;

@Local
public interface ShipmentService {
    Shipment createShipment(ShipmentDTO dto) throws ShipmentException, CustomsException;

    int processBatchShipmentUpdate(List<Long> shipmentIds, ShipmentStatus status);

    Shipment getShipmentByTrackingNumber(String trackingNumber);
}
