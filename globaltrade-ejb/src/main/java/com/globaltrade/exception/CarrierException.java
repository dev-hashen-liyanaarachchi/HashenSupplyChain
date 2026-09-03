package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class CarrierException extends GlobalTradeException {

    private final String carrierName;

    public CarrierException(String carrierName, String reason) {
        super("Carrier Communication Failure [" + carrierName + "]: " + reason);
        this.carrierName = carrierName;
    }

    public String getCarrierName() {
        return carrierName;
    }
}
