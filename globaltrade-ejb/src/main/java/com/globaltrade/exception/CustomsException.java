package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CustomsException extends GlobalTradeException {

    private final String hsCode;

    public CustomsException(String hsCode, String reason) {
        super("Customs Compliance Violation for HS Code [" + hsCode + "]: " + reason);
        this.hsCode = hsCode;
    }

    public String getHsCode() {
        return hsCode;
    }
}
