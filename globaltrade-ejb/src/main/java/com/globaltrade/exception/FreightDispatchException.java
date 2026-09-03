package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class FreightDispatchException extends ShipmentException {

    public FreightDispatchException(String trackingNumber, String reason) {
        super(trackingNumber, reason);
    }
}
