package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class ShipmentException extends GlobalTradeException {

    private final String trackingNumber;

    public ShipmentException(String trackingNumber, String reason) {
        super("Shipment Disruption for Tracking #" + trackingNumber + ": " + reason);
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() { return trackingNumber; }
}
