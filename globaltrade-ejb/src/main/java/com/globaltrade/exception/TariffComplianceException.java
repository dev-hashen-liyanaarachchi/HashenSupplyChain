package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TariffComplianceException extends GlobalTradeException {

    private final String hsCode;

    public TariffComplianceException(String hsCode, String reason) {
        super("Customs Tariff Compliance Violation for HS Code [" + hsCode + "]: " + reason);
        this.hsCode = hsCode;
    }

    public String getHsCode() {
        return hsCode;
    }
}
